package com.linkbit.mvp.dto;

import lombok.Data;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ExtensionRequestDTO {
    @NotNull(message = "New tenure is required")
    @Min(value = 1, message = "New tenure must be at least 1 month")
    @com.fasterxml.jackson.annotation.JsonProperty("newTenureMonths")
    private Integer newTenureMonths;

    @DecimalMin(value = "0.00", message = "Interest rate cannot be negative")
    @DecimalMax(value = "100.00", message = "Interest rate cannot exceed 100")
    private BigDecimal newInterestRate;

    private String reason;
}
