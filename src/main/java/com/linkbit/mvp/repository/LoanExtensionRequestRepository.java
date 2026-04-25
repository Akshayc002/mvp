package com.linkbit.mvp.repository;

import com.linkbit.mvp.domain.LoanExtensionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface LoanExtensionRequestRepository extends JpaRepository<LoanExtensionRequest, UUID> {
    Optional<LoanExtensionRequest> findFirstByLoanIdAndStatusOrderByCreatedAtDesc(UUID loanId, LoanExtensionRequest.ExtensionStatus status);
}
