package com.linkbit.mvp.repository;

import com.linkbit.mvp.domain.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, UUID> {
    List<LoanRepayment> findByLoanId(UUID loanId);
    List<LoanRepayment> findByLoanIdOrderByCreatedAtDesc(UUID loanId);
    List<LoanRepayment> findByStatusOrderByCreatedAtDesc(com.linkbit.mvp.domain.RepaymentStatus status);
}
