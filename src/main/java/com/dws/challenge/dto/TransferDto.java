package com.dws.challenge.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TransferDto {
    @NotNull
    @NotEmpty
    private final String fromAccountId;
    @NotNull
    @NotEmpty
    private final String toAccountId;
    @NotNull
    @Min(value = 0, message = "Initial balance must be positive.")
    private BigDecimal amount;
}
