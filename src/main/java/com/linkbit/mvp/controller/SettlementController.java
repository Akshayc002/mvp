package com.linkbit.mvp.controller;

import com.linkbit.mvp.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping("/loans/{loanId}/settlement/confirm")
    public ResponseEntity<Void> confirmSettlement(@PathVariable UUID loanId, Authentication authentication) {
        settlementService.confirmSettlement(loanId, authentication.getName());
        return ResponseEntity.ok().build();
    }
}
