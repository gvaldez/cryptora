package com.orbesource.cryptora.model.enums;

import java.util.List;
import java.util.stream.Stream;


public enum Ticker {

    /**
     * Indicates the option to incorporate additional cryptocurrencies.
     */

    TON, //
	BTC, // (Bitcoin): Most liquid and widely traded cryptocurrency.
	ETH, // (Ethereum): High liquidity, strong ecosystem, and frequent price action.
	BNB, // (Binance Coin): Tied to Binance, useful for reduced trading fees on the platform.
	SOL, // (Solana): Known for scalability and fast transaction speeds, popular for day traders.
	XRP, // (Ripple): Often traded due to volatility and news-driven price movements.
	ADA, // (Cardano): Frequently traded for its development updates and ecosystem growth.
	MATIC, // (Polygon): Popular in DeFi and Layer-2 scaling solutions.
	DOGE, // (Dogecoin) & SHIB (Shiba Inu): Meme coins with unpredictable price swings, ideal for high-risk, high-reward strategies.
	LTC, // (Litecoin): Often used as a "silver to Bitcoin's gold."
	DOT; // (Polkadot): Focused on interoperability, often experiences swings tied to ecosystem updates.*/
    
    public String getSymbol() 
    {
        return this.name() + "USDT";
    }

    public static List<String> getAllSymbols() 
    {
        return Stream.of(Ticker.values()).map(Ticker::getSymbol).toList();
    }
}


