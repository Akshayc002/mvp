package com.linkbit.mvp.controller;

import com.linkbit.mvp.dto.ChatMessage;
import com.linkbit.mvp.dto.SendChatMessageRequest;
import com.linkbit.mvp.repository.UserRepository;
import com.linkbit.mvp.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(@RequestParam UUID loanId) {
        return ResponseEntity.ok(chatService.getMessages(loanId));
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendMessage(
            Authentication authentication,
            @Valid @RequestBody SendChatMessageRequest request) {
        
        var user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        chatService.sendMessage(request.getLoanId(), user.getId(), request.getMessageText());
        return ResponseEntity.ok().build();
    }
}
