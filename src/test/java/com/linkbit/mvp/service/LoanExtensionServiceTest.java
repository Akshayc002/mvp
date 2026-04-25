package com.linkbit.mvp.service;

import com.linkbit.mvp.domain.*;
import com.linkbit.mvp.dto.ExtensionRequestDTO;
import com.linkbit.mvp.repository.LoanExtensionRequestRepository;
import com.linkbit.mvp.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanExtensionServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanExtensionRequestRepository extensionRequestRepository;

    @Mock
    private StateMachineService stateMachineService;

    @InjectMocks
    private LoanExtensionService loanExtensionService;

    private User borrower;
    private User lender;
    private Loan loan;
    private UUID loanId;

    @BeforeEach
    void setUp() {
        loanId = UUID.randomUUID();
        borrower = User.builder().id(UUID.randomUUID()).build();
        lender = User.builder().id(UUID.randomUUID()).build();
        loan = Loan.builder()
                .id(loanId)
                .borrower(borrower)
                .lender(lender)
                .status(LoanStatus.ACTIVE)
                .tenureDays(30)
                .interestRate(new BigDecimal("10.00"))
                .build();
    }

    @Test
    void requestExtension_Success() {
        ExtensionRequestDTO dto = new ExtensionRequestDTO();
        dto.setNewTenureDays(60);
        dto.setNewInterestRate(new BigDecimal("12.00"));
        dto.setReason("Need more time");

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(extensionRequestRepository.existsByLoanIdAndStatus(loanId, LoanExtensionRequest.ExtensionStatus.PENDING))
                .thenReturn(false);

        loanExtensionService.requestExtension(loanId, dto, borrower);

        verify(extensionRequestRepository, times(1)).save(any(LoanExtensionRequest.class));
        verify(stateMachineService, times(1)).transition(loan, LoanAction.REQUEST_EXTENSION, ActorType.BORROWER);
    }

    @Test
    void requestExtension_Failure_NotActive() {
        loan.setStatus(LoanStatus.REPAID);
        ExtensionRequestDTO dto = new ExtensionRequestDTO();
        dto.setNewTenureDays(60);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        assertThrows(IllegalStateException.class, () ->
                loanExtensionService.requestExtension(loanId, dto, borrower)
        );
    }

    @Test
    void requestExtension_Failure_DuplicatePendingRequest() {
        ExtensionRequestDTO dto = new ExtensionRequestDTO();
        dto.setNewTenureDays(60);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(extensionRequestRepository.existsByLoanIdAndStatus(loanId, LoanExtensionRequest.ExtensionStatus.PENDING))
                .thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                loanExtensionService.requestExtension(loanId, dto, borrower)
        );
    }

    @Test
    void requestExtension_Failure_NotBorrower() {
        User otherUser = User.builder().id(UUID.randomUUID()).build();
        ExtensionRequestDTO dto = new ExtensionRequestDTO();

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        assertThrows(IllegalStateException.class, () -> 
            loanExtensionService.requestExtension(loanId, dto, otherUser)
        );
    }

    @Test
    void respondToExtension_Approve_Success() {
        LoanExtensionRequest request = LoanExtensionRequest.builder()
                .loan(loan)
                .newTenureDays(60)
                .newInterestRate(new BigDecimal("12.00"))
                .status(LoanExtensionRequest.ExtensionStatus.PENDING)
                .build();

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(extensionRequestRepository.findFirstByLoanIdAndStatusOrderByCreatedAtDesc(loanId, LoanExtensionRequest.ExtensionStatus.PENDING))
                .thenReturn(Optional.of(request));

        loanExtensionService.respondToExtension(loanId, true, lender);

        assertEquals(60, loan.getTenureDays());
        assertEquals(new BigDecimal("12.00"), loan.getInterestRate());
        assertEquals(LoanExtensionRequest.ExtensionStatus.APPROVED, request.getStatus());
        verify(stateMachineService, times(1)).transition(loan, LoanAction.APPROVE_EXTENSION, ActorType.LENDER);
    }

    @Test
    void respondToExtension_Reject_Success() {
        LoanExtensionRequest request = LoanExtensionRequest.builder()
                .loan(loan)
                .status(LoanExtensionRequest.ExtensionStatus.PENDING)
                .build();

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(extensionRequestRepository.findFirstByLoanIdAndStatusOrderByCreatedAtDesc(loanId, LoanExtensionRequest.ExtensionStatus.PENDING))
                .thenReturn(Optional.of(request));

        loanExtensionService.respondToExtension(loanId, false, lender);

        assertEquals(LoanExtensionRequest.ExtensionStatus.REJECTED, request.getStatus());
        verify(stateMachineService, times(1)).transition(loan, LoanAction.REJECT_EXTENSION, ActorType.LENDER);
    }

    @Test
    void respondToExtension_Failure_NotLender() {
        User otherUser = User.builder().id(UUID.randomUUID()).build();
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        assertThrows(IllegalStateException.class, () -> 
            loanExtensionService.respondToExtension(loanId, true, otherUser)
        );
    }

    @Test
    void respondToExtension_Failure_NoPendingRequest() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(extensionRequestRepository.findFirstByLoanIdAndStatusOrderByCreatedAtDesc(loanId, LoanExtensionRequest.ExtensionStatus.PENDING))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> 
            loanExtensionService.respondToExtension(loanId, true, lender)
        );
    }

    @Test
    void requestExtension_Failure_LoanNotFound() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> 
            loanExtensionService.requestExtension(loanId, new ExtensionRequestDTO(), borrower)
        );
    }

    @Test
    void respondToExtension_Failure_LoanNotFound() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> 
            loanExtensionService.respondToExtension(loanId, true, lender)
        );
    }
}
