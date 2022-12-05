package com.nextjedi.trading.tipbasedtrading.token;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.User;
import com.zerodhatech.ticker.KiteTicker;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@RestController
public class TokenList {
    @GetMapping(path="/token")
    public String getTokens() throws IOException, KiteException {
        String apikey = "2himf7a1ff5edpjy";
        String apiSecret = "n3u0lp8xjwqwkhf3ljvmrwhr1o8b9f2k";
        System.out.println( "Hello World!" );
        // Initialize Kiteconnect using apiKey.
        KiteConnect kiteSdk = new KiteConnect(apikey);

// Set userId.
        kiteSdk.setUserId("your_userId");

/* First you should get request_token, public_token using kitconnect login and then use request_token, public_token, api_secret to make any kiteconnect api call.
Get login url. Use this url in webview to login user, after authenticating user you will get requestToken. Use the same to get accessToken. */
        String url = kiteSdk.getLoginURL();

        String requestToken = "i3svyy0mQGyZzQBcI9ffroqjWtXuJO14";
// Get accessToken as follows,
//        User user =  kiteSdk.generateSession(requestToken, apiSecret);

        String accessToken="FEVV7yn8arZPFbcINzCVFSXbpHfILPfC";
        String publicToken="EHfbIYBKR43r5gW1R9536Sx4rhWyqrL6";
// Set request token and public token which are obtained from login process.
        kiteSdk.setAccessToken(accessToken);
        kiteSdk.setPublicToken(publicToken);

        List<Instrument> instruments = kiteSdk.getInstruments();
        KiteTicker tickerProvider = new KiteTicker(kiteSdk.getAccessToken(), kiteSdk.getApiKey());
        List<Long> tokens = instruments.stream().map(instrument -> instrument.getInstrument_token()).collect(Collectors.toList());
        tickerProvider.setMode((ArrayList<Long>) tokens, KiteTicker.modeLTP);



        return "getting tokens";
    }
}
