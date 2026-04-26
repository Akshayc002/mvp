package com.linkbit.mvp.dto;

import com.linkbit.mvp.domain.RepaymentType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTermsRequest {

    @NotNull
    @DecimalMin(value = "1000.00", message = "Principal must be at least 1000")
    @com.fasterxml.jackson.annotation.JsonProperty("principalAmount")
    private BigDecimal principalAmount;

    @NotNull
    @DecimalMin(value = "0.00", message = "Interest rate cannot be negative")
    @DecimalMax(value = "100.00", message = "Interest rate cannot exceed 100")
    @com.fasterxml.jackson.annotation.JsonProperty("interestRate")
    private BigDecimal interestRate;

    @NotNull
    @Min(value = 1, message = "Tenure must be at least 1 month")
    @com.fasterxml.jackson.annotation.JsonProperty("tenureMonths")
    private Integer tenureMonths;

    @NotNull
    @com.fasterxml.jackson.annotation.JsonProperty("repaymentType")
    private RepaymentType repaymentType;

    @NotNull
    @Min(value = 1, message = "EMI count must be at least 1")
    @com.fasterxml.jackson.annotation.JsonProperty("emiCount")
    private Integer emiCount;

    @NotNull
    @Min(value = 40, message = "Expected LTV must be at least 40")
    @Max(value = 60, message = "Expected LTV cannot exceed 60")
    @com.fasterxml.jackson.annotation.JsonProperty("expectedLtvPercent")
    private Integer expectedLtvPercent;

    @NotNull
    @Min(value = 1, message = "Margin call LTV must be at least 1")
    @Max(value = 95, message = "Margin call LTV cannot exceed 95")
    @com.fasterxml.jackson.annotation.JsonProperty("marginCallLtvPercent")
    private Integer marginCallLtvPercent;

    @NotNull
    @Min(value = 1, message = "Liquidation LTV must be at least 1")
    @Max(value = 99, message = "Liquidation LTV cannot exceed 99")
    @com.fasterxml.jackson.annotation.JsonProperty("liquidationLtvPercent")
    private Integer liquidationLtvPercent;
}
