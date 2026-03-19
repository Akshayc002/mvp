package com.linkbit.mvp.security;

import com.linkbit.mvp.domain.*;
import com.linkbit.mvp.repository.*;
import com.linkbit.mvp.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class OfferConcurrencyTest {

    @Autowired
    private LoanMarketplaceService marketplaceService;

    @Autowired
    private NegotiationService negotiationService;

    @Autowired
    private LoanOfferRepository loanOfferRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    private User lender;
    private User borrower1;
    private User borrower2;
    private LoanOffer offer;

    @BeforeEach
    void setUp() {
        loanRepository.deleteAll();
        loanOfferRepository.deleteAll();
        userRepository.deleteAll();

        lender = userRepository.save(User.builder()
                .email("lender@test.com")
                .password("pass")
                .pseudonym("Lender")
                .kycStatus(KycStatus.VERIFIED)
                .build());

        borrower1 = userRepository.save(User.builder()
                .email("borrower1@test.com")
                .password("pass")
                .pseudonym("B1")
                .kycStatus(KycStatus.VERIFIED)
                .build());

        borrower2 = userRepository.save(User.builder()
                .email("borrower2@test.com")
                .password("pass")
                .pseudonym("B2")
                .kycStatus(KycStatus.VERIFIED)
                .build());

        offer = loanOfferRepository.save(LoanOffer.builder()
                .lender(lender)
                .loanAmountInr(new BigDecimal("10000"))
                .interestRate(new BigDecimal("10"))
                .tenureDays(30)
                .expectedLtvPercent(50)
                .status(LoanOfferStatus.OPEN)
                .build());
    }

    @Test
    void concurrentConnectOffer_ShouldOnlyAllowOneActiveNegotiation() throws InterruptedException {
        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        executor.submit(() -> {
            try {
                latch.await();
                marketplaceService.connectOffer(borrower1.getEmail(), offer.getId());
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            }
        });

        executor.submit(() -> {
            try {
                latch.await();
                marketplaceService.connectOffer(borrower2.getEmail(), offer.getId());
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            }
        });

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertEquals(1, successCount.get(), "Exactly one connect request should succeed");
        assertEquals(1, failureCount.get(), "One connect request should fail");
        assertEquals(1, loanRepository.count(), "Only one loan should be created");
    }

    @Test
    void concurrentFinalizeContract_ShouldOnlyAllowOneSuccess() throws InterruptedException {
        // First, create two negotiations (this requires bypass or deliberate setup if connect is already guarded)
        // We'll create them manually to test the 'finalize' guard
        Loan loan1 = loanRepository.save(Loan.builder()
                .offer(offer)
                .lender(lender)
                .borrower(borrower1)
                .status(LoanStatus.NEGOTIATING)
                .principalAmount(offer.getLoanAmountInr())
                .interestRate(offer.getInterestRate())
                .tenureDays(offer.getTenureDays())
                .repaymentType(RepaymentType.BULLET)
                .expectedLtvPercent(50)
                .marginCallLtvPercent(80)
                .liquidationLtvPercent(95)
                .build());

        Loan loan2 = loanRepository.save(Loan.builder()
                .offer(offer)
                .lender(lender)
                .borrower(borrower2)
                .status(LoanStatus.NEGOTIATING)
                .principalAmount(offer.getLoanAmountInr())
                .interestRate(offer.getInterestRate())
                .tenureDays(offer.getTenureDays())
                .repaymentType(RepaymentType.BULLET)
                .expectedLtvPercent(50)
                .marginCallLtvPercent(80)
                .liquidationLtvPercent(95)
                .build());

        assertEquals(LoanOfferStatus.OPEN, offer.getStatus());

        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        executor.submit(() -> {
            try {
                latch.await();
                negotiationService.finalizeContract(lender.getEmail(), loan1.getId());
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            }
        });

        executor.submit(() -> {
            try {
                latch.await();
                negotiationService.finalizeContract(lender.getEmail(), loan2.getId());
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            }
        });

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertEquals(1, successCount.get(), "Exactly one finalize request should succeed");
        assertEquals(1, failureCount.get(), "One finalize request should fail");
        
        LoanOffer updatedOffer = loanOfferRepository.findById(offer.getId()).get();
        assertEquals(LoanOfferStatus.CLOSED, updatedOffer.getStatus());
    }
}
