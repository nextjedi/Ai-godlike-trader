package com.nextjedi.trading.tipbasedtrading.util;

import com.nextjedi.trading.tipbasedtrading.models.SecretModel;

import java.util.HashMap;
import java.util.Map;

import static com.nextjedi.trading.tipbasedtrading.util.Constants.USER_ID;

public class ApiSecret {
    private ApiSecret(){}
    public static final Map<String, SecretModel> apiKeys = new HashMap<>();

    static {
        apiKeys.put(USER_ID, new SecretModel("2himf7a1ff5edpjy","87mebxtvu3226igmjnkjfjfcrgiphfxb","Ap@240392","KZHIZCXRM5OL3XJUFL7EAPJQOJ6H5HH2"));
        apiKeys.put("FHS049", new SecretModel("qxx4bvrmb0iw6bb1","b6r8eil0vuzrti7c8vkzenzpsm2mb85g","Ardorbrother@11","YFUKTD6FYVIK6TH2OHZHKEMZPH3MONVV"));
    }
}
