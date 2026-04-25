package com.linkbit.mvp.service;

import com.linkbit.mvp.domain.*;
import com.linkbit.mvp.dto.ExtensionRequestDTO;
import com.linkbit.mvp.repository.LoanExtensionRequestRepository;
import com.linkbit.mvp.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanExtensionService {

    private final LoanRepository loanRepository;
    private final LoanExtensionRequestRepository extensionRequestRepository;
    private final StateMachineService stateMachineService;

    @Transactional
    public void requestExtension(UUID loanId, ExtensionRequestDTO dto, User borrower) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        if (!loan.getBorrower().getId().equals(borrower.getId())) {
            throw new IllegalStateException("Only the borrower can request an extension");
        }
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new IllegalStateException("Extensions can only be requested for ACTIVE loans");
        }
        if (extensionRequestRepository.existsByLoanIdAndStatus(loanId, LoanExtensionRequest.ExtensionStatus.PENDING)) {
            throw new IllegalStateException("A pending extension request already exists for this loan");
        }

        LoanExtensionRequest request = LoanExtensionRequest.builder()
                .loan(loan)
                .newTenureDays(dto.getNewTenureDays())
                .newInterestRate(dto.getNewInterestRate())
                .reason(dto.getReason())
                .status(LoanExtensionRequest.ExtensionStatus.PENDING)
                .build();

        extensionRequestRepository.save(request);
        stateMachineService.transition(loan, LoanAction.REQUEST_EXTENSION, ActorType.BORROWER);
    }

    @Transactional
    public void respondToExtension(UUID loanId, boolean approve, User lender) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        if (!loan.getLender().getId().equals(lender.getId())) {
            throw new IllegalStateException("Only the lender can respond to an extension request");
        }

        LoanExtensionRequest request = extensionRequestRepository.findFirstByLoanIdAndStatusOrderByCreatedAtDesc(loanId, LoanExtensionRequest.ExtensionStatus.PENDING)
                .orElseThrow(() -> new IllegalStateException("No pending extension request found"));

        if (approve) {
            request.setStatus(LoanExtensionRequest.ExtensionStatus.APPROVED);
            loan.setTenureDays(request.getNewTenureDays());
            if (request.getNewInterestRate() != null) {
                loan.setInterestRate(request.getNewInterestRate());
            }
            stateMachineService.transition(loan, LoanAction.APPROVE_EXTENSION, ActorType.LENDER);
        } else {
            request.setStatus(LoanExtensionRequest.ExtensionStatus.REJECTED);
            stateMachineService.transition(loan, LoanAction.REJECT_EXTENSION, ActorType.LENDER);
        }

        extensionRequestRepository.save(request);
    }
}
