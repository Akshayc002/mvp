package com.linkbit.mvp.service;

import com.linkbit.mvp.domain.Loan;
import com.linkbit.mvp.domain.LoanStatus;
import com.linkbit.mvp.domain.User;
import com.linkbit.mvp.repository.LoanRepository;
import com.linkbit.mvp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final CollateralReleaseService collateralReleaseService;
    private final ChatService chatService;

    @Transactional
    public void confirmSettlement(UUID loanId, String email) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (loan.getStatus() != LoanStatus.REPAID) {
            throw new IllegalStateException("Settlement can only be confirmed for REPAID loans");
        }

        boolean isBorrower = loan.getBorrower().getId().equals(user.getId());
        boolean isLender = loan.getLender().getId().equals(user.getId());

        if (!isBorrower && !isLender) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized: Only participants can confirm settlement");
        }

        if (isBorrower) {
            if (Boolean.TRUE.equals(loan.getBorrowerSettlementConfirmed())) {
                log.info("Borrower already confirmed settlement for loan {}", loanId);
                return;
            }
            loan.setBorrowerSettlementConfirmed(true);
            chatService.sendSystemMessage(loanId, "SYSTEM: Borrower has confirmed the settlement.");
        } else {
            if (Boolean.TRUE.equals(loan.getLenderSettlementConfirmed())) {
                log.info("Lender already confirmed settlement for loan {}", loanId);
                return;
            }
            loan.setLenderSettlementConfirmed(true);
            chatService.sendSystemMessage(loanId, "SYSTEM: Lender has confirmed the settlement.");
        }

        loanRepository.save(loan);

        if (Boolean.TRUE.equals(loan.getBorrowerSettlementConfirmed()) && Boolean.TRUE.equals(loan.getLenderSettlementConfirmed())) {
            log.info("Both parties confirmed settlement for loan {}. Releasing collateral.", loanId);
            chatService.sendSystemMessage(loanId, "SYSTEM: Both parties confirmed settlement. Releasing collateral...");
            collateralReleaseService.releaseCollateral(loanId, "admin@linkbit.com");
        }
    }
}
