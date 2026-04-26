package com.linkbit.mvp.dto;

import com.linkbit.mvp.domain.LoanStatus;
import com.linkbit.mvp.domain.RepaymentType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class LoanSummaryResponse {
    private UUID loanId;
    private String role;
    private LoanStatus status;
    private String borrowerPseudonym;
    private String lenderPseudonym;
    private String counterpartyPseudonym;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    @com.fasterxml.jackson.annotation.JsonProperty("tenureMonths")
    private Integer tenureMonths;
    private RepaymentType repaymentType;
    private Integer emiCount;
    private BigDecimal emiAmount;
    private BigDecimal totalRepaymentAmount;
    private BigDecimal totalOutstanding;
    private Integer expectedLtvPercent;
    private BigDecimal currentLtvPercent;
    private BigDecimal collateralBtcAmount;
    private BigDecimal collateralValueInr;
    private String agreementHash;
    private String disbursementReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
