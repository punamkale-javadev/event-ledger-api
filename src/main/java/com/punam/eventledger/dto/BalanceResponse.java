package com.punam.eventledger.dto;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {

    private String accountId;
    private BigDecimal balance;
}
