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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class GlobalInvariantsTest {

    @Autowired
    private StateMachineService stateMachineService;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LoanOfferRepository loanOfferRepository;

    @Autowired
    private UserRepository userRepository;

    private Loan loan;

    @BeforeEach
    void setUp() {
        loanRepository.deleteAll();
        loanOfferRepository.deleteAll();
        userRepository.deleteAll();

        User borrower = userRepository.save(User.builder()
                .email("borrower@test.com")
                .password("pass")
                .pseudonym("B")
                .kycStatus(KycStatus.VERIFIED)
                .build());

        User lender = userRepository.save(User.builder()
                .email("lender@test.com")
                .password("pass")
                .pseudonym("L")
                .kycStatus(KycStatus.VERIFIED)
                .build());

        LoanOffer offer = loanOfferRepository.save(LoanOffer.builder()
                .lender(lender)
                .loanAmountInr(new BigDecimal("10000"))
                .interestRate(new BigDecimal("10"))
                .tenureDays(30)
                .expectedLtvPercent(50)
                .status(LoanOfferStatus.OPEN)
                .build());

        loan = loanRepository.save(Loan.builder()
                .offer(offer)
                .borrower(borrower)
                .lender(lender)
                .principalAmount(new BigDecimal("10000"))
                .interestRate(new BigDecimal("10"))
                .tenureDays(30)
                .status(LoanStatus.NEGOTIATING)
                .principalOutstanding(new BigDecimal("10000"))
                .totalOutstanding(new BigDecimal("10000"))
                .collateralBtcAmount(new BigDecimal("0.01"))
                .build());
    }

    @Test
    void invariant1_NoDisbursementBeforeCollateralLocked() {
        // Current state: NEGOTIATING
        // Try DISBURSE_FIAT (which should only work from COLLATERAL_LOCKED)
        // Note: Even if we hacked the transition map, the invariant check should catch it
        assertThrows(IllegalStateException.class, () -> 
            stateMachineService.transition(loan, LoanAction.DISBURSE_FIAT, ActorType.BORROWER)
        );
    }

    @Test
    void invariant2_NoClosureWithOutstandingBalance() {
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setPrincipalOutstanding(new BigDecimal("5000"));
        loanRepository.save(loan);

        // Try to close (via RELEASE_COLLATERAL) while principal is outstanding
        assertThrows(IllegalStateException.class, () -> 
            stateMachineService.transition(loan, LoanAction.RELEASE_COLLATERAL, ActorType.ADMIN)
        );
    }

    @Test
    void invariant4_NoTransitionsFromTerminalStates() {
        loan.setStatus(LoanStatus.CLOSED);
        loanRepository.save(loan);

        // Try any transition from CLOSED
        assertThrows(IllegalStateException.class, () -> 
            stateMachineService.transition(loan, LoanAction.FINALIZE_CONTRACT, ActorType.ADMIN)
        );
    }

    @Test
    void invariant5_ActiveLoansMustHaveCollateral() {
        loan.setStatus(LoanStatus.COLLATERAL_LOCKED);
        loan.setCollateralBtcAmount(BigDecimal.ZERO);
        loanRepository.save(loan);

        // Try to transition to ACTIVE without collateral
        assertThrows(IllegalStateException.class, () -> 
            stateMachineService.transition(loan, LoanAction.DISBURSE_FIAT, ActorType.SYSTEM)
        );
    }

    @Test
    void invariant3_NoLiquidationWithoutEligibility() {
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setCollateralBtcAmount(new BigDecimal("0.01")); // Value = ~60k INR
        loan.setPrincipalAmount(new BigDecimal("10000")); // LTV = ~16% (Safe)
        loan.setTotalOutstanding(new BigDecimal("10000"));
        loanRepository.save(loan);

        // Try to trigger LTV_DROP_LIQUIDATION while LTV is safe
        assertThrows(IllegalStateException.class, () -> 
            stateMachineService.transition(loan, LoanAction.LTV_DROP_LIQUIDATION, ActorType.SYSTEM)
        );
    }

    @Test
    @Transactional
    void shouldAllowValidTransitions() {
        loan.setStatus(LoanStatus.COLLATERAL_LOCKED);
        loan.setCollateralBtcAmount(new BigDecimal("0.01"));
        loan.setPrincipalOutstanding(new BigDecimal("10000"));
        loanRepository.save(loan);

        // Valid DISBURSE_FIAT from COLLATERAL_LOCKED
        assertDoesNotThrow(() -> 
            stateMachineService.transition(loan, LoanAction.DISBURSE_FIAT, ActorType.SYSTEM)
        );
    }
}
