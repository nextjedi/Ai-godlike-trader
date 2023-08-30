package com.nextjedi.trading.tipbasedtrading.util;

import com.nextjedi.trading.tipbasedtrading.models.ApiSecret;
import com.nextjedi.trading.tipbasedtrading.models.TokenAccess;
import com.nextjedi.trading.tipbasedtrading.service.TokenService;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.ticker.KiteTicker;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.nextjedi.trading.tipbasedtrading.models.Constants.USER_ID;
@Component
@Data
public class KiteUtil {
    private KiteTicker kiteTicker;
    private KiteConnect kiteConnect;

    @Autowired
    private TokenService tokenService;
    public KiteConnect connectToKite(){
        var secret = ApiSecret.apiKeys.get(USER_ID);
        TokenAccess tokenAccess=tokenService.getLatestTokenByUserId(USER_ID);
        com.zerodhatech.kiteconnect.KiteConnect kiteSdk = new com.zerodhatech.kiteconnect.KiteConnect(secret.getApiKey());
        kiteSdk.setAccessToken(tokenAccess.getAccessToken());
        kiteSdk.setPublicToken(tokenAccess.getPublicToken());
        return kiteSdk;
    }
    public KiteTicker getKiteTicker(KiteConnect kiteConnect){
        return new KiteTicker(kiteConnect.getAccessToken(), kiteConnect.getApiKey());
    }
    public void establishTickConnection(){
//        todo schedule the connection establishment on market days only
        kiteConnect = connectToKite();
        kiteTicker = getKiteTicker(kiteConnect);
    }
    private boolean isProperlyConnected(){
        if (Objects.nonNull(kiteConnect) && Objects.nonNull(kiteTicker)){
//            todo check if connection exists
            return true;
        }
        return false;
    }
//    todo token automation selenium

}
