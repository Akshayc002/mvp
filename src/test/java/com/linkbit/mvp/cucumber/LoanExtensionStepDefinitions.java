package com.linkbit.mvp.cucumber;

import com.linkbit.mvp.domain.*;
import com.linkbit.mvp.dto.ExtensionRequestDTO;
import com.linkbit.mvp.repository.LoanExtensionRequestRepository;
import com.linkbit.mvp.repository.LoanRepository;
import com.linkbit.mvp.service.LoanExtensionService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.linkbit.mvp.repository.LoanOfferRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@CucumberContextConfiguration
@SpringBootTest
@ActiveProfiles("test")
public class LoanExtensionStepDefinitions {

    @Autowired
    private LoanExtensionService extensionService;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LoanExtensionRequestRepository extensionRequestRepository;

    @Autowired
    private com.linkbit.mvp.repository.UserRepository userRepository;

    @Autowired
    private LoanOfferRepository loanOfferRepository;

    private Map<String, User> users = new HashMap<>();
    private Loan currentLoan;
    private Exception lastException;

    @Before
    public void cleanup() {
        extensionRequestRepository.deleteAll();
        loanRepository.deleteAll();
        loanOfferRepository.deleteAll();
        userRepository.deleteAll();
        users.clear();
        lastException = null;
    }

    @Given("a borrower named {string} and a lender named {string}")
    public void setupUsers(String borrowerName, String lenderName) {
        User borrower = User.builder()
                .id(UUID.randomUUID())
                .email(borrowerName.toLowerCase() + "@example.com")
                .password("encoded_password")
                .pseudonym(borrowerName)
                .role(ActorType.USER)
                .build();
        User lender = User.builder()
                .id(UUID.randomUUID())
                .email(lenderName.toLowerCase() + "@example.com")
                .password("encoded_password")
                .pseudonym(lenderName)
                .role(ActorType.USER)
                .build();
        users.put(borrowerName, userRepository.save(borrower));
        users.put(lenderName, userRepository.save(lender));
    }

    @Given("an active loan exists between {string} and {string} with {int} days tenure")
    public void setupLoan(String borrowerName, String lenderName, int tenure) {
        User borrower = users.get(borrowerName);
        User lender = users.get(lenderName);

        LoanOffer offer = LoanOffer.builder()
                .lender(lender)
                .loanAmountInr(new BigDecimal("100000"))
                .interestRate(new BigDecimal("10.00"))
                .expectedLtvPercent(50)
                .tenureDays(tenure)
                .status(LoanOfferStatus.OPEN)
                .build();
        offer = loanOfferRepository.save(offer);

        currentLoan = Loan.builder()
                .offer(offer)
                .borrower(borrower)
                .lender(lender)
                .status(LoanStatus.ACTIVE)
                .tenureDays(tenure)
                .principalAmount(new BigDecimal("100000"))
                .interestRate(new BigDecimal("10.00"))
                .collateralBtcAmount(new BigDecimal("1.0")) // Invariant check in StateMachine
                .build();
        currentLoan = loanRepository.save(currentLoan);
    }

    @When("{string} requests an extension of {int} days with {int}% interest")
    public void requestExtension(String userName, int tenure, int interest) {
        User user = users.get(userName);
        ExtensionRequestDTO dto = new ExtensionRequestDTO();
        dto.setNewTenureDays(tenure);
        dto.setNewInterestRate(new BigDecimal(interest));
        dto.setReason("Test extension");

        try {
            extensionService.requestExtension(currentLoan.getId(), dto, user);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Given("{string} has a pending extension request for {int} days")
    public void setupPendingRequest(String borrowerName, int tenure) {
        ExtensionRequestDTO dto = new ExtensionRequestDTO();
        dto.setNewTenureDays(tenure);
        requestExtension(borrowerName, tenure, 12);
    }

    @When("{string} approves the extension request")
    public void approveExtension(String userName) {
        User user = users.get(userName);
        try {
            extensionService.respondToExtension(currentLoan.getId(), true, user);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("{string} rejects the extension request")
    public void rejectExtension(String userName) {
        User user = users.get(userName);
        try {
            extensionService.respondToExtension(currentLoan.getId(), false, user);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("{string} tries to request an extension for {string}'s loan")
    public void unauthorizedRequest(String unauthorizedUser, String borrowerName) {
        requestExtension(unauthorizedUser, 60, 12);
    }

    @Then("the loan status should be {string}")
    public void verifyLoanStatus(String status) {
        Loan updated = loanRepository.findById(currentLoan.getId()).get();
        assertEquals(LoanStatus.valueOf(status), updated.getStatus());
    }

    @Then("a pending extension request should exist for the loan")
    public void verifyPendingRequest() {
        assertTrue(extensionRequestRepository.findFirstByLoanIdAndStatusOrderByCreatedAtDesc(currentLoan.getId(), LoanExtensionRequest.ExtensionStatus.PENDING).isPresent());
    }

    @Then("the loan tenure should be {int} days")
    public void verifyTenure(int tenure) {
        Loan updated = loanRepository.findById(currentLoan.getId()).get();
        assertEquals(tenure, updated.getTenureDays());
    }

    @Then("the extension request status should be {string}")
    public void verifyRequestStatus(String status) {
        LoanExtensionRequest request = extensionRequestRepository.findAll().stream()
                .filter(r -> r.getLoan().getId().equals(currentLoan.getId()))
                .findFirst().get();
        assertEquals(status, request.getStatus().name());
    }

    @Then("the request should be denied with an error {string}")
    public void verifyError(String errorMessage) {
        assertNotNull(lastException);
        assertEquals(errorMessage, lastException.getMessage());
    }
}
