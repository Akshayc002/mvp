package com.linkbit.mvp.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminOverviewResponse {
    private Metrics metrics;
    private List<LoanDetailResponse> loans;

    @Data
    @Builder
    public static class Metrics {
        private long totalLoans;
        private long pendingFees;
        private long pendingRepayments;
        private long activeRiskCases;
        private long collateralAwaitingVerification;
    }
}
