package com.tukaloff.tradeterminal.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TradePosition {

    private UUID uuid = UUID.randomUUID();
    private String ticker;
    private boolean isOpen;
    private String openDate;
    private BigDecimal openValue;
    private BigDecimal openFeePerc;
    private BigDecimal openFeeValue;
    private BigDecimal closeFeePerc;
    private BigDecimal closeFeeValue;
    private BigDecimal minimalSellPrice;
    private String closeDate;
    private BigDecimal factSellPrice;
    private BigDecimal profit;
}
