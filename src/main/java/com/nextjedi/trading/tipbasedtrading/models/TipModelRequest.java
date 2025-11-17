package com.nextjedi.trading.tipbasedtrading.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TipModelRequest {

    @NotNull(message = "Instrument is required")
    @Valid
    private InstrumentQuery instrument;

    @Positive(message = "Price must be positive")
    private int price;

    @Positive(message = "Stop loss must be positive")
    private int stopLoss;

    @Positive(message = "Target must be positive")
    private int target;

    @NotNull(message = "Trade type is required")
    private TradeType type;
}
