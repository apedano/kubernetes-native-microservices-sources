package org.acme.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OverdraftLimitUpdate {
    private Long accountNumber;
    private BigDecimal newOverdraftLimit;

}
