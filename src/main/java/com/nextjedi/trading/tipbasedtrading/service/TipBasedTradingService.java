package com.nextjedi.trading.tipbasedtrading.service;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.Tick;
import com.zerodhatech.ticker.KiteTicker;
import com.zerodhatech.ticker.OnConnect;
import com.zerodhatech.ticker.OnOrderUpdate;
import com.zerodhatech.ticker.OnTicks;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TipBasedTradingService {
//    actually execute trade

    public void tipBasedTrading(){

        String apikey = "2himf7a1ff5edpjy";
        String apiSecret = "n3u0lp8xjwqwkhf3ljvmrwhr1o8b9f2k";
        KiteConnect kiteSdk = new KiteConnect(apikey);
        kiteSdk.setUserId("your_userId");

        String accessToken="FEVV7yn8arZPFbcINzCVFSXbpHfILPfC";
        String publicToken="EHfbIYBKR43r5gW1R9536Sx4rhWyqrL6";
// Set request token and public token which are obtained from login process.
        kiteSdk.setAccessToken(accessToken);
        kiteSdk.setPublicToken(publicToken);
        KiteTicker tickerProvider = new KiteTicker(kiteSdk.getAccessToken(), kiteSdk.getApiKey());
        ArrayList<Instrument> instruments = new ArrayList<>();
        List<Long> tokens = instruments.stream().map(instrument -> instrument.getInstrument_token()).collect(Collectors.toList());
        tickerProvider.setMode((ArrayList<Long>) tokens,KiteTicker.modeLTP);
        tickerProvider.setOnConnectedListener(new OnConnect() {
            @Override
            public void onConnected() {
                /** Subscribe ticks for token.
                 * By default, all tokens are subscribed for modeQuote.
                 * */
                tickerProvider.subscribe((ArrayList<Long>) tokens);
                tickerProvider.setMode((ArrayList<Long>) tokens, KiteTicker.modeFull);
            }
        });
        tickerProvider.setOnOrderUpdateListener(new OnOrderUpdate() {
            @Override
            public void onOrderUpdate(Order order) {
                System.out.println("order update "+order.orderId);
            }
        });
        tickerProvider.setOnTickerArrivalListener(new OnTicks() {
            @Override
            public void onTicks(ArrayList<Tick> ticks) {
                NumberFormat formatter = new DecimalFormat();
                System.out.println("ticks size "+ticks.size());
                if(ticks.size() > 0) {
                    System.out.println("last price "+ticks.get(0).getLastTradedPrice());
                    System.out.println("open interest "+formatter.format(ticks.get(0).getOi()));
                    System.out.println("day high OI "+formatter.format(ticks.get(0).getOpenInterestDayHigh()));
                    System.out.println("day low OI "+formatter.format(ticks.get(0).getOpenInterestDayLow()));
                    System.out.println("change "+formatter.format(ticks.get(0).getChange()));
                    System.out.println("tick timestamp "+ticks.get(0).getTickTimestamp());
                    System.out.println("tick timestamp date "+ticks.get(0).getTickTimestamp());
                    System.out.println("last traded time "+ticks.get(0).getLastTradedTime());
                    System.out.println(ticks.get(0).getMarketDepth().get("buy").size());
                }
            }
        });
        tickerProvider.connect();

    }


}
