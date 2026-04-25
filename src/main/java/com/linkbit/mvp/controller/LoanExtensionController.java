package com.linkbit.mvp.controller;

import com.linkbit.mvp.domain.User;
import com.linkbit.mvp.dto.ExtensionRequestDTO;
import com.linkbit.mvp.service.AuthService;
import com.linkbit.mvp.service.LoanExtensionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanExtensionController {

    private final LoanExtensionService extensionService;
    private final AuthService authService;

    @PostMapping("/{loanId}/extension")
    public ResponseEntity<Void> requestExtension(
            @PathVariable UUID loanId,
            @RequestBody ExtensionRequestDTO dto) {
        User currentUser = authService.getCurrentUser();
        extensionService.requestExtension(loanId, dto, currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{loanId}/extension/approve")
    public ResponseEntity<Void> approveExtension(@PathVariable UUID loanId) {
        User currentUser = authService.getCurrentUser();
        extensionService.respondToExtension(loanId, true, currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{loanId}/extension/reject")
    public ResponseEntity<Void> rejectExtension(@PathVariable UUID loanId) {
        User currentUser = authService.getCurrentUser();
        extensionService.respondToExtension(loanId, false, currentUser);
        return ResponseEntity.ok().build();
    }
}
