package com.linkbit.mvp.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ExtensionRequestDTO {
    private Integer newTenureDays;
    private BigDecimal newInterestRate;
    private String reason;
}
