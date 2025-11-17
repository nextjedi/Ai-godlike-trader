package com.nextjedi.trading.tipbasedtrading.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.ToString;

import java.sql.Date;

@Data
@ToString
public class InstrumentQuery {

    @Positive(message = "Strike price must be positive")
    private int strike;

    @NotBlank(message = "Instrument name is required")
    private String name;

    @NotBlank(message = "Instrument type is required")
    private String instrumentType;

    @NotNull(message = "Expiry date is required")
    private Date expiry;

}
