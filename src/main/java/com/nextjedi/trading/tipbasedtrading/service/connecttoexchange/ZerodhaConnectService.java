package com.nextjedi.trading.tipbasedtrading.service.connecttoexchange;

import com.nextjedi.trading.tipbasedtrading.models.TokenAccess;
import com.nextjedi.trading.tipbasedtrading.service.TokenService;
import com.nextjedi.trading.tipbasedtrading.util.ApiSecret;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.ticker.KiteTicker;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.nextjedi.trading.tipbasedtrading.util.Constants.USER_ID;

@Data
@Service
@Slf4j
public class ZerodhaConnectService {
    @Autowired
    private TokenService tokenService;

    private KiteTicker kiteTicker;
    private KiteConnect kiteConnect;
    public void connectToKite(){
        var secret = ApiSecret.apiKeys.get(USER_ID);
        TokenAccess tokenAccess=tokenService.getLatestTokenByUserId(USER_ID);
        if(Objects.isNull(kiteConnect)){
            kiteConnect = new KiteConnect(secret.getApiKey());
        }
        kiteConnect.setAccessToken(tokenAccess.getAccessToken());
        kiteConnect.setPublicToken(tokenAccess.getPublicToken());
    }
    public KiteTicker getKiteTicker(){
        if(Objects.isNull(kiteConnect)){
//            if not connected establish connection to the token
            connectToKite();
        }
        if(!(Objects.nonNull(kiteTicker) && kiteTicker.isConnectionOpen())){
            kiteTicker = new KiteTicker(kiteConnect.getAccessToken(), kiteConnect.getApiKey());
        }
        return kiteTicker;
    }
//    todo refresh token when required
    public KiteConnect getKiteConnect(){
        if(Objects.isNull(kiteConnect)){
//            if not connected establish connection to the token
            connectToKite();
        }
        return kiteConnect;
    }
}
