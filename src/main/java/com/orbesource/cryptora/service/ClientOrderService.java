package com.orbesource.cryptora.service;

import static com.binance.api.client.domain.account.NewOrder.limitBuy;
import static com.binance.api.client.domain.account.NewOrder.limitSell;
import static com.binance.api.client.domain.account.NewOrder.marketBuy;
import static com.binance.api.client.domain.account.NewOrder.marketSell;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.request.CancelOrderRequest;
import com.binance.api.client.domain.account.request.CancelOrderResponse;
import com.binance.api.client.domain.account.request.OrderRequest;
import com.orbesource.cryptora.model.dto.ClientBalance;

import jakarta.annotation.PostConstruct;

@Service
public class ClientOrderService {

	
    private static final Logger logger = LoggerFactory.getLogger(ClientOrderService.class);

    
    
    public static final String SELL_ORDER_SIDE = OrderSide.SELL.name();
    public static final String BUY_ORDER_SIDE = OrderSide.BUY.name();
    public static final String BTC = "BTC";
    public static final String USDT = "USDT";
    public static final String ORDER_UNAVAILABLE_MSG = ">>> No current order available.";
    public static final String UNEXPECTED_MSG = ">>> Unexpected case for order.";
    public static final String WAITING_MSG = ">>> Waiting till enough data points ... ";
    public static final String BUY_ORDER_MSG = ">>> BUY order created with orderId : ";
    public static final String SELL_ORDER_MSG = ">>> SELL order created with orderId : ";
    public static final String ORDER_CANCELLED_MSG = ">>> Order Canceled with status : ";

    
    
	
    @Value("${binance.api.key}")
    private String apiKey;
    @Value("${binance.api.secret}")
    private String apiSecret;
    @Value("${cryptora.current.trade.pair}")
    private String currentTradePair;
    @Value("${cryptora.enter.price}")
    private double enterPriceParam;
    @Value("${cryptora.exit.price}")
    private double exitPriceParam;
    @Value("${cryptora.exit.strategy.percentage}")
    private double exitLevel;

    @Autowired
    private BinanceApiRestClient binanceApiRestClient;

    @PostConstruct
    public void postConstruct() 
    {

        logger.info(">>> *** GUS BOT 2023 *** \n Current settings : currentTradePair = " + currentTradePair
                + "; \nenterPriceParam + " + enterPriceParam
                + "; \nexitPriceParam = " + exitPriceParam
                + "; \nexit level percentage = " + exitLevel + "%;");

        initCheckPing();
    }

    private void initCheckPing() 
    {
        if (apiKey.isEmpty() || apiSecret.isEmpty()) 
        {
            throw new RuntimeException("No credentials");
        }

        try 
        {
        	binanceApiRestClient.ping();
        	
        	logger.info("getServerTime: " + binanceApiRestClient.getServerTime());
        	
        	binanceApiRestClient.getAccount();
        } 
        catch (com.binance.api.client.exception.BinanceApiException apiException) 
        {
            logger.info("BinanceApiException message: " + apiException.getError().getMsg());
            logger.info("Cause: " + apiException.getCause());
            throw new RuntimeException(apiException.getError().getMsg(), apiException.getCause());
        } 
        catch (Exception exception) 
        {
            logger.info("Cause: " + exception.getCause());
            throw new RuntimeException("Exception when first api call");
        }
    }

    public String createMarketBuy(String quantity) 
    {
        logger.info(">>> Creating MARKET_BUY order ");
        return createOrder(marketBuy(currentTradePair, quantity));
    }

    public String createMarketSell(String quantity) 
    {
        logger.info(">>> Creating MARKET_SELL order ");
        return createOrder(marketSell(currentTradePair, quantity));
    }

    public String createLimitBuy(String quantity, String price) 
    {
        logger.info(">>> Creating LIMIT_BUY order ");
        return createOrder(limitBuy(currentTradePair, TimeInForce.GTC, quantity, price));
    }

    public String createLimitSell(String quantity, String price) 
    {
        logger.info(">>> Creating LIMIT_SELL order ");
        return createOrder(limitSell(currentTradePair, TimeInForce.GTC, quantity, price));
    }

    public ClientBalance getBalanceForCurrency(String currency) 
    {
        AssetBalance balance = binanceApiRestClient.getAccount().getAssetBalance(currency);
        return new ClientBalance(balance.getAsset(), balance.getFree(), balance.getLocked());
    }


    public String getCurrentPrice(String currentTradePair) 
    {
        return binanceApiRestClient.getPrice(currentTradePair).getPrice();
    }

    public List<Order> getOpenOrders(String currentTradePair) 
    {
        OrderRequest orderRequest = new OrderRequest(currentTradePair);
        return binanceApiRestClient.getOpenOrders(orderRequest);
    }

    public String cancelOrder(String currentTradePair, String clientOrderId) 
    {

        CancelOrderRequest orderRequest = new CancelOrderRequest(currentTradePair, clientOrderId);
        CancelOrderResponse orderResponse = binanceApiRestClient.cancelOrder(orderRequest);
        logger.info(ORDER_CANCELLED_MSG + orderResponse.toString());
        return orderResponse.toString();
    }

    private String createOrder(NewOrder order) 
    {

        NewOrderResponse newOrderResponse = binanceApiRestClient.newOrder(order);

        logger.info(">>> New Order status : " + newOrderResponse.getStatus());
        if (newOrderResponse.getStatus() == OrderStatus.REJECTED) {
            logger.info(">>> newOrderResponse REJECTED ");
        }

        logger.info(">>> Order created with status : " + newOrderResponse.getStatus() +
                " with price : " + newOrderResponse.getPrice());

        return newOrderResponse.getClientOrderId();
    }
}
