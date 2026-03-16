package com.linkbit.mvp.controller;

import com.linkbit.mvp.dto.AdminOverviewResponse;
import com.linkbit.mvp.dto.LoanDetailResponse;
import com.linkbit.mvp.dto.LoanSummaryResponse;
import com.linkbit.mvp.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/loans/mine")
    public ResponseEntity<List<LoanSummaryResponse>> getMyLoans(Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getMyLoans(authentication.getName()));
    }

    @GetMapping("/loans/{loanId}/details")
    public ResponseEntity<LoanDetailResponse> getLoanDetails(
            Authentication authentication,
            @PathVariable UUID loanId) {
        return ResponseEntity.ok(dashboardService.getLoanDetail(authentication.getName(), loanId));
    }

    @GetMapping("/admin/overview")
    public ResponseEntity<AdminOverviewResponse> getAdminOverview() {
        return ResponseEntity.ok(dashboardService.getAdminOverview());
    }
}
