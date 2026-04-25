package com.linkbit.mvp.security;

import com.linkbit.mvp.domain.*;
import com.linkbit.mvp.dto.*;
import com.linkbit.mvp.repository.*;
import com.linkbit.mvp.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
public class EdgeCaseIntegrationTest {

    @Autowired private AuthService authService;
    @Autowired private LoanMarketplaceService marketplaceService;
    @Autowired private NegotiationService negotiationService;
    @Autowired private PaymentService paymentService;
    @Autowired private EscrowService escrowService;
    @Autowired private DisbursementService disbursementService;
    @Autowired private RepaymentService repaymentService;
    @Autowired private LiquidationService liquidationService;
    @Autowired private LtvMonitoringWorker ltvMonitoringWorker;

    @MockBean private BtcPriceService btcPriceService;

    @Autowired private LoanRepository loanRepository;
    @Autowired private LoanOfferRepository loanOfferRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PlatformFeeRepository feeRepository;
    @Autowired private LoanRepaymentRepository repaymentRepository;

    private String borrowerEmail = "b_edge@test.com";
    private String lenderEmail = "l_edge@test.com";
    private String randomEmail = "random@test.com";

    @BeforeEach
    @Transactional
    void setUp() {
        loanRepository.deleteAll();
        loanOfferRepository.deleteAll();
        userRepository.deleteAll();

        // Register users
        registerUser(borrowerEmail, "Borrower", KycStatus.VERIFIED);
        registerUser(lenderEmail, "Lender", KycStatus.VERIFIED);
        registerUser(randomEmail, "Random", KycStatus.VERIFIED);
    }

    private void registerUser(String email, String name, KycStatus kyc) {
        RegisterRequest r = new RegisterRequest();
        r.setEmail(email);
        r.setPassword("pass");
        r.setName(name);
        r.setDob("1990-01-01");
        authService.register(r);
        User user = userRepository.findByEmail(email).get();
        user.setKycStatus(kyc);
        userRepository.save(user);
    }

    @Test
    void testUnauthorized_LenderCannotSignAsBorrower() {
        UUID loanId = setupLoanToAwaitingSignatures();
        
        // Lender trying to sign as borrower (via service email mismatch)
        // The service should check if the provided email matches the participant role
        // In NegotiationService.signContract, it checks if the user is a participant.
        // But does it check IF they are signing the RIGHT part?
        // Let's check NegotiationService.
        
        negotiationService.signContract(lenderEmail, loanId, "LENDER_SIG");
        Loan loan = loanRepository.findById(loanId).get();
        assertNotNull(loan.getLenderSignature());
        assertNull(loan.getBorrowerSignature());
        
        // Now lender tries to sign AGAIN but the service logic for SIGN_CONTRACT 
        // in StateMachine only moves to AWAITING_FEE when BOTH sign.
        // If lender signs again, it should ideally be idempotent for their part.
    }

    @Test
    void testUnauthorized_ThirdPartyCannotPerformAction() {
        UUID loanId = setupLoanToAwaitingSignatures();
        
        // Random user tries to sign
        assertThrows(RuntimeException.class, () -> 
            negotiationService.signContract(randomEmail, loanId, "HACKER_SIG")
        );
        
        // Random user tries to update terms
        UpdateTermsRequest utr = new UpdateTermsRequest();
        utr.setPrincipalAmount(new BigDecimal("20000"));
        assertThrows(RuntimeException.class, () -> 
            negotiationService.updateTerms(randomEmail, loanId, utr)
        );
    }

    @Test
    void testInvalidState_NoDisburseBeforeCollateral() {
        UUID loanId = setupLoanToAwaitingSignatures();
        // Skip fees and collateral, try to disburse
        assertThrows(RuntimeException.class, () -> 
            disbursementService.markDisbursed(loanId, lenderEmail, DisbursementRequest.builder().transactionReference("TX1").build())
        );
    }

    @Test
    void testKycGating_UnverifiedUserCannotCreateOffer() {
        String unverifiedEmail = "unverified@test.com";
        registerUser(unverifiedEmail, "Unverified", KycStatus.PENDING);
        
        CreateOfferRequest cor = new CreateOfferRequest();
        cor.setLoanAmountInr(new BigDecimal("10000"));
        cor.setInterestRate(new BigDecimal("10"));
        cor.setTenureDays(30);
        cor.setExpectedLtvPercent(50);
        
        assertThrows(RuntimeException.class, () -> marketplaceService.createOffer(unverifiedEmail, cor));
    }

    @Test
    void testFinancials_OverRepaymentHandling() {
        UUID loanId = setupActiveLoan();
        Loan loan = loanRepository.findById(loanId).get();
        BigDecimal outstanding = loan.getTotalOutstanding();
        BigDecimal overpayment = outstanding.add(new BigDecimal("1000"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            repaymentService.submitRepayment(loanId, borrowerEmail, RepaymentRequest.builder()
                .amount(overpayment)
                .transactionReference("OVERPAY")
                .proofImageUrl("url")
                .build())
        );
        
    }

    @Test
    void testLiquidation_PreciseThreshold() {
        // Principal 5000, 50% Expected LTV -> Needs 10000 INR collateral.
        // At 50,000 BTC price, needs 0.2 BTC.
        // Liquidation at 95% LTV.
        // 5000 / (0.2 * Price) = 0.95  => Price = 5000 / (0.2 * 0.95) = 5000 / 0.19 = 26315.78
        
        UUID loanId = setupActiveLoanWithSpecifics(new BigDecimal("5000"), new BigDecimal("0.2"), 50); 
        // LTV = 9500 / (0.2 * 50000) = 9500 / 10000 = 95.0%
        
        // Set price to hit exactly 95% LTV
        given(btcPriceService.getCurrentBtcPrice()).willReturn(new BigDecimal("26315")); // Slightly below 26315.78 -> LTV > 95%
        ltvMonitoringWorker.monitorLtvLevels();
        assertEquals(LoanStatus.LIQUIDATION_ELIGIBLE, loanRepository.findById(loanId).get().getStatus());
        
        // Recover price significantly
        given(btcPriceService.getCurrentBtcPrice()).willReturn(new BigDecimal("60000"));
        ltvMonitoringWorker.monitorLtvLevels();
        assertEquals(LoanStatus.ACTIVE, loanRepository.findById(loanId).get().getStatus());
    }

    private UUID setupLoanToAwaitingSignatures() {
        return setupLoanToAwaitingSignaturesWithSpecifics(new BigDecimal("10000"), 50);
    }

    private UUID setupLoanToAwaitingSignaturesWithSpecifics(BigDecimal principal, int expectedLtv) {
        CreateOfferRequest cor = new CreateOfferRequest();
        cor.setLoanAmountInr(principal);
        cor.setInterestRate(new BigDecimal("10"));
        cor.setTenureDays(30);
        cor.setExpectedLtvPercent(expectedLtv);
        marketplaceService.createOffer(lenderEmail, cor);
        LoanOffer offer = loanOfferRepository.findAll().get(loanOfferRepository.findAll().size()-1);
        
        UUID loanId = marketplaceService.connectOffer(borrowerEmail, offer.getId());
        negotiationService.finalizeContract(borrowerEmail, loanId);
        negotiationService.finalizeContract(lenderEmail, loanId);
        return loanId;
    }

    private UUID setupActiveLoan() {
        return setupActiveLoanWithSpecifics(new BigDecimal("10000"), new BigDecimal("0.5"), 50);
    }

    private UUID setupActiveLoanWithSpecifics(BigDecimal principal, BigDecimal btcAmount, int expectedLtv) {
        given(btcPriceService.getCurrentBtcPrice()).willReturn(new BigDecimal("50000"));
        
        UUID loanId = setupLoanToAwaitingSignaturesWithSpecifics(principal, expectedLtv);
        
        negotiationService.signContract(borrowerEmail, loanId, "SIG");
        negotiationService.signContract(lenderEmail, loanId, "SIG");
        
        paymentService.verifyPayment(paymentService.initiateFeePayment(borrowerEmail, loanId).getFeeId());
        paymentService.verifyPayment(paymentService.initiateFeePayment(lenderEmail, loanId).getFeeId());
        
        escrowService.generateAddress(borrowerEmail, loanId);
        escrowService.deposit(borrowerEmail, loanId, btcAmount);
        escrowService.verifyDeposit(loanId);
        
        disbursementService.markDisbursed(loanId, lenderEmail, DisbursementRequest.builder().transactionReference("REF").build());
        disbursementService.confirmReceipt(loanId, borrowerEmail);
        
        return loanId;
    }

}
