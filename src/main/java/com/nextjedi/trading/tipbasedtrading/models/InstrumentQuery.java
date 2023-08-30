package com.nextjedi.trading.tipbasedtrading.models;

import lombok.Data;
import lombok.ToString;

import java.sql.Date;

@Data
@ToString
public class InstrumentQuery {
    private int strike;
    private String name;
    private String instrumentType;
    private Date expiry;

}
