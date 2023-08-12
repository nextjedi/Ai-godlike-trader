package com.nextjedi.trading.tipbasedtrading.models;

import java.util.HashMap;

import static com.nextjedi.trading.tipbasedtrading.models.Constants.USER_ID;

public class ApiSecret {
    public static final HashMap<String, Secret> apiKeys = new HashMap<>();

    static {
        apiKeys.put(USER_ID, new Secret("2himf7a1ff5edpjy","87mebxtvu3226igmjnkjfjfcrgiphfxb"));
        apiKeys.put("FHS049", new Secret("qxx4bvrmb0iw6bb1","b6r8eil0vuzrti7c8vkzenzpsm2mb85g"));
    }
}
