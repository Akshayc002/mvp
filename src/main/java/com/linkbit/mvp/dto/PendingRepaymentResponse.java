package com.linkbit.mvp.dto;

import com.linkbit.mvp.domain.RepaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PendingRepaymentResponse {
    private UUID repaymentId;
    private BigDecimal amountInr;
    private String transactionReference;
    private String proofUrl;
    private RepaymentStatus status;
    private LocalDateTime createdAt;
}
