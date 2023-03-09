package com.nextjedi.trading.tipbasedtrading.util;

public class Helper {

    public static double tickMultiple(double price, double tickSize){
        return ((int)(price/tickSize))*tickSize;
    }
}
