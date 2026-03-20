package com.linkbit.mvp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SendChatMessageRequest {
    @NotNull
    @JsonProperty("loan_id")
    private UUID loanId;

    @NotBlank
    @JsonProperty("message_text")
    private String messageText;
}
