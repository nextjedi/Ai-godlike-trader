package com.nextjedi.trading.tipbasedtrading.broker.impl;

import com.nextjedi.trading.tipbasedtrading.broker.TickListener;
import com.nextjedi.trading.tipbasedtrading.broker.TradingBroker;
import com.nextjedi.trading.tipbasedtrading.broker.dto.*;
import com.nextjedi.trading.tipbasedtrading.events.EventPublisher;
import com.nextjedi.trading.tipbasedtrading.events.tick.TickReceivedEvent;
import com.nextjedi.trading.tipbasedtrading.models.TokenAccess;
import com.nextjedi.trading.tipbasedtrading.service.TokenService;
import com.nextjedi.trading.tipbasedtrading.util.ApiSecret;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.*;
import com.zerodhatech.ticker.KiteTicker;
import com.zerodhatech.ticker.OnConnect;
import com.zerodhatech.ticker.OnDisconnect;
import com.zerodhatech.ticker.OnTicks;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.nextjedi.trading.tipbasedtrading.util.Constants.USER_ID;

/**
 * Zerodha broker implementation using Kite Connect API
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ZerodhaBroker implements TradingBroker {

    private final TokenService tokenService;
    private final EventPublisher eventPublisher;

    @Value("${trading.brokers.zerodha.user-id:#{null}}")
    private String configuredUserId;

    private KiteConnect kiteConnect;
    private KiteTicker kiteTicker;
    private TickListener tickListener;
    private boolean connected = false;

    @PostConstruct
    public void init() {
        log.info("Initializing Zerodha Broker");
    }

    @Override
    public String getBrokerName() {
        return "zerodha";
    }

    @Override
    public void connect() {
        try {
            log.info("Connecting to Zerodha Kite...");
            String userId = configuredUserId != null ? configuredUserId : USER_ID;
            var secret = ApiSecret.apiKeys.get(userId);

            if (secret == null) {
                throw new IllegalStateException("No API secret found for user: " + userId);
            }

            TokenAccess tokenAccess = tokenService.getLatestTokenByUserId(userId);
            if (tokenAccess == null) {
                throw new IllegalStateException("No token found for user: " + userId);
            }

            if (kiteConnect == null) {
                kiteConnect = new KiteConnect(secret.getApiKey());
            }

            kiteConnect.setAccessToken(tokenAccess.getAccessToken());
            kiteConnect.setPublicToken(tokenAccess.getPublicToken());

            connected = true;
            log.info("Successfully connected to Zerodha Kite");
        } catch (Exception e) {
            log.error("Failed to connect to Zerodha: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to connect to Zerodha", e);
        }
    }

    @Override
    public void disconnect() {
        if (kiteTicker != null && kiteTicker.isConnectionOpen()) {
            kiteTicker.disconnect();
        }
        connected = false;
        log.info("Disconnected from Zerodha");
    }

    @Override
    public boolean isConnected() {
        return connected && kiteConnect != null;
    }

    @Override
    public OrderResponse placeOrder(OrderRequest request) {
        ensureConnected();
        try {
            OrderParams orderParams = new OrderParams();
            orderParams.exchange = request.getExchange();
            orderParams.tradingsymbol = request.getTradingSymbol();
            orderParams.transactionType = mapTransactionType(request.getTransactionType());
            orderParams.orderType = mapOrderType(request.getOrderType());
            orderParams.quantity = request.getQuantity();
            orderParams.price = request.getPrice();
            orderParams.triggerPrice = request.getTriggerPrice();
            orderParams.product = mapProduct(request.getProduct());
            orderParams.validity = mapValidity(request.getValidity());
            orderParams.tag = request.getTag();

            Order order = kiteConnect.placeOrder(orderParams, Constants.VARIETY_REGULAR);

            log.info("Order placed successfully: {}", order.orderId);
            return OrderResponse.builder()
                    .orderId(order.orderId)
                    .success(true)
                    .message("Order placed successfully")
                    .build();

        } catch (KiteException | IOException e) {
            log.error("Failed to place order: {}", e.getMessage(), e);
            return OrderResponse.builder()
                    .success(false)
                    .message("Failed to place order: " + e.getMessage())
                    .errorCode(e instanceof KiteException ? String.valueOf(((KiteException) e).code) : "ERROR")
                    .build();
        }
    }

    @Override
    public OrderResponse modifyOrder(String orderId, OrderRequest request) {
        ensureConnected();
        try {
            OrderParams orderParams = new OrderParams();
            orderParams.orderType = mapOrderType(request.getOrderType());
            orderParams.quantity = request.getQuantity();
            orderParams.price = request.getPrice();
            orderParams.triggerPrice = request.getTriggerPrice();

            Order order = kiteConnect.modifyOrder(orderId, orderParams, Constants.VARIETY_REGULAR);

            log.info("Order modified successfully: {}", orderId);
            return OrderResponse.builder()
                    .orderId(orderId)
                    .success(true)
                    .message("Order modified successfully")
                    .build();

        } catch (KiteException | IOException e) {
            log.error("Failed to modify order {}: {}", orderId, e.getMessage(), e);
            return OrderResponse.builder()
                    .orderId(orderId)
                    .success(false)
                    .message("Failed to modify order: " + e.getMessage())
                    .errorCode(e instanceof KiteException ? String.valueOf(((KiteException) e).code) : "ERROR")
                    .build();
        }
    }

    @Override
    public OrderResponse cancelOrder(String orderId) {
        ensureConnected();
        try {
            Order order = kiteConnect.cancelOrder(orderId, Constants.VARIETY_REGULAR);

            log.info("Order cancelled successfully: {}", orderId);
            return OrderResponse.builder()
                    .orderId(orderId)
                    .success(true)
                    .message("Order cancelled successfully")
                    .build();

        } catch (KiteException | IOException e) {
            log.error("Failed to cancel order {}: {}", orderId, e.getMessage(), e);
            return OrderResponse.builder()
                    .orderId(orderId)
                    .success(false)
                    .message("Failed to cancel order: " + e.getMessage())
                    .errorCode(e instanceof KiteException ? String.valueOf(((KiteException) e).code) : "ERROR")
                    .build();
        }
    }

    @Override
    public OrderDetail getOrder(String orderId) {
        ensureConnected();
        try {
            List<Order> orders = kiteConnect.getOrders();
            Order order = orders.stream()
                    .filter(o -> o.orderId.equals(orderId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

            return mapToOrderDetail(order);

        } catch (KiteException | IOException e) {
            log.error("Failed to get order {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to get order", e);
        }
    }

    @Override
    public List<OrderDetail> getOrders() {
        ensureConnected();
        try {
            List<Order> orders = kiteConnect.getOrders();
            return orders.stream()
                    .map(this::mapToOrderDetail)
                    .collect(Collectors.toList());

        } catch (KiteException | IOException e) {
            log.error("Failed to get orders: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get orders", e);
        }
    }

    @Override
    public List<Position> getPositions() {
        ensureConnected();
        try {
            Map<String, List<com.zerodhatech.models.Position>> positions = kiteConnect.getPositions();
            List<com.zerodhatech.models.Position> netPositions = positions.get("net");

            if (netPositions == null) {
                return Collections.emptyList();
            }

            return netPositions.stream()
                    .map(this::mapToPosition)
                    .collect(Collectors.toList());

        } catch (KiteException | IOException e) {
            log.error("Failed to get positions: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get positions", e);
        }
    }

    @Override
    public Balance getBalance() {
        ensureConnected();
        try {
            Margins margins = kiteConnect.getMargins("equity");

            return Balance.builder()
                    .available(margins.available.cash)
                    .utilized(margins.utilised.debits)
                    .total(margins.net)
                    .build();

        } catch (KiteException | IOException e) {
            log.error("Failed to get balance: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get balance", e);
        }
    }

    @Override
    public List<Instrument> getInstruments(String exchange) {
        ensureConnected();
        try {
            List<com.zerodhatech.models.Instrument> instruments = kiteConnect.getInstruments(exchange);

            return instruments.stream()
                    .map(this::mapToInstrument)
                    .collect(Collectors.toList());

        } catch (KiteException | IOException e) {
            log.error("Failed to get instruments for exchange {}: {}", exchange, e.getMessage(), e);
            throw new RuntimeException("Failed to get instruments", e);
        }
    }

    @Override
    public Instrument getInstrument(Long instrumentToken) {
        ensureConnected();
        try {
            // Zerodha doesn't have direct API to get instrument by token
            // We need to search through all instruments (inefficient but works)
            // TODO: Implement caching for instruments
            List<com.zerodhatech.models.Instrument> instruments = kiteConnect.getInstruments();

            return instruments.stream()
                    .filter(i -> i.getInstrument_token().equals(instrumentToken))
                    .findFirst()
                    .map(this::mapToInstrument)
                    .orElseThrow(() -> new IllegalArgumentException("Instrument not found: " + instrumentToken));

        } catch (KiteException | IOException e) {
            log.error("Failed to get instrument {}: {}", instrumentToken, e.getMessage(), e);
            throw new RuntimeException("Failed to get instrument", e);
        }
    }

    @Override
    public void subscribeToTicks(List<Long> instrumentTokens, TickListener listener) {
        ensureConnected();
        this.tickListener = listener;

        if (kiteTicker == null || !kiteTicker.isConnectionOpen()) {
            initializeTickerConnection();
        }

        ArrayList<Long> tokens = new ArrayList<>(instrumentTokens);
        kiteTicker.subscribe(tokens);
        kiteTicker.setMode(tokens, KiteTicker.modeFull);

        log.info("Subscribed to {} instruments", instrumentTokens.size());
    }

    @Override
    public void unsubscribeFromTicks(List<Long> instrumentTokens) {
        if (kiteTicker != null && kiteTicker.isConnectionOpen()) {
            ArrayList<Long> tokens = new ArrayList<>(instrumentTokens);
            kiteTicker.unsubscribe(tokens);
            log.info("Unsubscribed from {} instruments", instrumentTokens.size());
        }
    }

    @Override
    public String getUserId() {
        return configuredUserId != null ? configuredUserId : USER_ID;
    }

    // Helper methods

    private void ensureConnected() {
        if (!isConnected()) {
            connect();
        }
    }

    private void initializeTickerConnection() {
        kiteTicker = new KiteTicker(kiteConnect.getAccessToken(), kiteConnect.getApiKey());

        kiteTicker.setOnConnectedListener(new OnConnect() {
            @Override
            public void onConnected() {
                log.info("Zerodha WebSocket connected");
                if (tickListener != null) {
                    tickListener.onConnected();
                }
            }
        });

        kiteTicker.setOnDisconnectedListener(new OnDisconnect() {
            @Override
            public void onDisconnected() {
                log.info("Zerodha WebSocket disconnected");
                if (tickListener != null) {
                    tickListener.onDisconnected();
                }
            }
        });

        kiteTicker.setOnTickerArrivalListener(new OnTicks() {
            @Override
            public void onTicks(ArrayList<com.zerodhatech.models.Tick> ticks) {
                List<Tick> mappedTicks = ticks.stream()
                        .map(ZerodhaBroker.this::mapToTick)
                        .collect(Collectors.toList());

                // Publish tick events
                mappedTicks.forEach(tick -> {
                    TickReceivedEvent event = TickReceivedEvent.builder()
                            .instrumentToken(tick.getInstrumentToken())
                            .tradingSymbol(tick.getTradingSymbol())
                            .lastPrice(tick.getLastPrice())
                            .volume(tick.getVolume())
                            .bidPrice(tick.getBidPrice())
                            .askPrice(tick.getAskPrice())
                            .openInterest(tick.getOpenInterest())
                            .tickTimestamp(tick.getTimestamp())
                            .build();
                    eventPublisher.publish(event);
                });

                if (tickListener != null) {
                    tickListener.onTicksReceived(mappedTicks);
                }
            }
        });

        kiteTicker.setOnErrorListener(exception -> {
            log.error("Zerodha WebSocket error: {}", exception.getMessage(), exception);
            if (tickListener != null) {
                tickListener.onError(exception);
            }
        });

        kiteTicker.connect();
    }

    // Mapping methods

    private String mapTransactionType(OrderRequest.TransactionType type) {
        return type == OrderRequest.TransactionType.BUY ?
                Constants.TRANSACTION_TYPE_BUY : Constants.TRANSACTION_TYPE_SELL;
    }

    private String mapOrderType(OrderRequest.OrderType type) {
        switch (type) {
            case MARKET: return Constants.ORDER_TYPE_MARKET;
            case LIMIT: return Constants.ORDER_TYPE_LIMIT;
            case SL: return Constants.ORDER_TYPE_SL;
            case SL_M: return Constants.ORDER_TYPE_SLM;
            default: throw new IllegalArgumentException("Unknown order type: " + type);
        }
    }

    private String mapProduct(OrderRequest.Product product) {
        switch (product) {
            case MIS: return Constants.PRODUCT_MIS;
            case NRML: return Constants.PRODUCT_NRML;
            case CNC: return Constants.PRODUCT_CNC;
            default: throw new IllegalArgumentException("Unknown product: " + product);
        }
    }

    private String mapValidity(OrderRequest.Validity validity) {
        return validity == OrderRequest.Validity.DAY ?
                Constants.VALIDITY_DAY : Constants.VALIDITY_IOC;
    }

    private OrderDetail mapToOrderDetail(Order order) {
        return OrderDetail.builder()
                .orderId(order.orderId)
                .parentOrderId(order.parentOrderId)
                .exchange(order.exchange)
                .tradingSymbol(order.tradingSymbol)
                .instrumentToken(order.instrumentToken)
                .transactionType(order.transactionType.equals(Constants.TRANSACTION_TYPE_BUY) ?
                        OrderRequest.TransactionType.BUY : OrderRequest.TransactionType.SELL)
                .orderType(mapFromZerodhaOrderType(order.orderType))
                .product(mapFromZerodhaProduct(order.product))
                .quantity(order.quantity)
                .filledQuantity(order.filledQuantity)
                .pendingQuantity(order.pendingQuantity)
                .price(order.price)
                .triggerPrice(order.triggerPrice)
                .averagePrice(order.averagePrice)
                .status(mapOrderStatus(order.status))
                .statusMessage(order.statusMessage)
                .orderTimestamp(Instant.parse(order.orderTimestamp))
                .exchangeTimestamp(order.exchangeTimestamp != null ? Instant.parse(order.exchangeTimestamp) : null)
                .tag(order.tag)
                .build();
    }

    private OrderRequest.OrderType mapFromZerodhaOrderType(String orderType) {
        switch (orderType) {
            case Constants.ORDER_TYPE_MARKET: return OrderRequest.OrderType.MARKET;
            case Constants.ORDER_TYPE_LIMIT: return OrderRequest.OrderType.LIMIT;
            case Constants.ORDER_TYPE_SL: return OrderRequest.OrderType.SL;
            case Constants.ORDER_TYPE_SLM: return OrderRequest.OrderType.SL_M;
            default: return OrderRequest.OrderType.MARKET;
        }
    }

    private OrderRequest.Product mapFromZerodhaProduct(String product) {
        switch (product) {
            case Constants.PRODUCT_MIS: return OrderRequest.Product.MIS;
            case Constants.PRODUCT_NRML: return OrderRequest.Product.NRML;
            case Constants.PRODUCT_CNC: return OrderRequest.Product.CNC;
            default: return OrderRequest.Product.MIS;
        }
    }

    private OrderDetail.OrderStatus mapOrderStatus(String status) {
        switch (status.toUpperCase()) {
            case "OPEN":
            case "PENDING":
                return OrderDetail.OrderStatus.OPEN;
            case "COMPLETE":
                return OrderDetail.OrderStatus.COMPLETE;
            case "CANCELLED":
                return OrderDetail.OrderStatus.CANCELLED;
            case "REJECTED":
                return OrderDetail.OrderStatus.REJECTED;
            case "MODIFY PENDING":
                return OrderDetail.OrderStatus.MODIFY_PENDING;
            case "CANCEL PENDING":
                return OrderDetail.OrderStatus.CANCEL_PENDING;
            case "TRIGGER PENDING":
                return OrderDetail.OrderStatus.TRIGGER_PENDING;
            default:
                return OrderDetail.OrderStatus.OPEN;
        }
    }

    private Position mapToPosition(com.zerodhatech.models.Position position) {
        return Position.builder()
                .exchange(position.exchange)
                .tradingSymbol(position.tradingSymbol)
                .instrumentToken(position.instrumentToken)
                .product(mapFromZerodhaProduct(position.product))
                .quantity(position.netQuantity)
                .buyQuantity(position.buyQuantity)
                .sellQuantity(position.sellQuantity)
                .averagePrice(position.averagePrice)
                .buyPrice(position.buyPrice)
                .sellPrice(position.sellPrice)
                .lastPrice(position.lastPrice)
                .pnl(position.pnl)
                .unrealizedPnl(position.unrealised)
                .value(position.value)
                .buyValue(position.buyValue)
                .sellValue(position.sellValue)
                .build();
    }

    private Instrument mapToInstrument(com.zerodhatech.models.Instrument instrument) {
        return Instrument.builder()
                .instrumentToken(instrument.getInstrument_token())
                .tradingSymbol(instrument.getTradingsymbol())
                .name(instrument.getName())
                .exchange(instrument.getExchange())
                .segment(instrument.getSegment())
                .instrumentType(instrument.getInstrument_type())
                .strike(instrument.getStrike())
                .expiry(instrument.getExpiry() != null ?
                        instrument.getExpiry().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null)
                .tickSize(instrument.getTick_size())
                .lotSize(instrument.getLot_size())
                .exchangeToken(instrument.getExchange_token())
                .build();
    }

    private Tick mapToTick(com.zerodhatech.models.Tick tick) {
        return Tick.builder()
                .instrumentToken(tick.getInstrumentToken())
                .tradingSymbol(tick.getTradingSymbol())
                .lastPrice(tick.getLastTradedPrice())
                .lastQuantity(tick.getLastTradedQuantity())
                .volume(tick.getVolumeTradedToday())
                .averageTradePrice(tick.getAverageTradePrice())
                .buyQuantity(tick.getTotalBuyQuantity())
                .sellQuantity(tick.getTotalSellQuantity())
                .openPrice(tick.getOpenPrice())
                .highPrice(tick.getHighPrice())
                .lowPrice(tick.getLowPrice())
                .closePrice(tick.getClosePrice())
                .bidPrice(tick.getDepth() != null && tick.getDepth().buy != null && !tick.getDepth().buy.isEmpty() ?
                        tick.getDepth().buy.get(0).getPrice() : null)
                .askPrice(tick.getDepth() != null && tick.getDepth().sell != null && !tick.getDepth().sell.isEmpty() ?
                        tick.getDepth().sell.get(0).getPrice() : null)
                .bidQuantity(tick.getDepth() != null && tick.getDepth().buy != null && !tick.getDepth().buy.isEmpty() ?
                        tick.getDepth().buy.get(0).getQuantity() : null)
                .askQuantity(tick.getDepth() != null && tick.getDepth().sell != null && !tick.getDepth().sell.isEmpty() ?
                        tick.getDepth().sell.get(0).getQuantity() : null)
                .openInterest(tick.getOi())
                .oiDayHigh(tick.getOiDayHigh())
                .oiDayLow(tick.getOiDayLow())
                .timestamp(Instant.now())
                .exchangeTimestamp(tick.getTickTimestamp() != null ?
                        tick.getTickTimestamp().toInstant() : null)
                .build();
    }
}
