# Cryptora-Analyze-Service

## **Description**

Cryptora-Analyze-Service is a Spring Boot application that tracks cryptocurrencies in real-time using the Binance API. It
monitors price trends, analyzing growth and decline, and provides users with real-time data to make informed decisions
based on market movements and analytics.

---

## **Key Features**

- **Real-time Data Fetching**: The application integrates with the Binance API to retrieve live cryptocurrency data.
  Users can track any available cryptocurrencies.

- **Cryptocurrency History Storage**: Redis is used to store the history of cryptocurrency prices, allowing users to
  analyze historical growth and decline trends.

- **Price Trend Analysis**: With stored price data, users can perform trend analysis on cryptocurrency growth and
  decline, supported by the Ta4j library.

---

## **Technologies**

- **Java**: The primary programming language.

- **Spring Boot**: Framework used for building the service.

- **MongoDB**: A NoSQL database used for storing real-time cryptocurrency quotes.

- **Redis**: A NoSQL storage solution used for saving cryptocurrency price history (prices and timestamps).

- **Binance API**: External API for retrieving up-to-date cryptocurrency price information in real-time.

- **Ta4j**: A library used for analyzing price movements, including growth and decline of cryptocurrencies.

- **Docker**: Containerization platform that helps package the application with its dependencies, ensuring consistent
  environments and simplifying deployment.

---

## **Installation Guide**

### **Prerequisites**

- Java 17 or higher
- Docker

### **Installation and Startup Steps**

1. **Clone the Repository**
   ```bash
   git clone https://github.com/dzenthai/Cryptora-Analyze-Service.git
   cd Cryptora-Analyze-Service
   ```

2. **Add Environment Variables**
   Create an .env file and add the necessary environment variables such as Binance API key and secret, along with Redis
   and MongoDB configurations.

3. **Build the Project Using Gradle**
   ```bash
   ./gradlew build
   ```

4. **Run the Application Using Docker**
   ```bash
   docker-compose up --build
   ```
   
---

## **Additional Information**

### **Changing Cryptocurrency Pairs**

In the `FetchService` class, you can change the cryptocurrency pairs by modifying the line:
   ```java
   List<String> symbols = List.of("BTCUSDT", "YourCryptoPair");
```

### **Adjusting Data Fetching Interval**

To modify the interval at which the application fetches and analyzes price data, update the following line in the `AppScheduler` class:

```java
@Scheduled(fixedRate = 10000) // Time is in milliseconds
public void executeInSequence() {
}
```

Also, it is recommended to adjust the duration in the `AnalyticService` class to match, as shown below:
```java
Bar bar = new BaseBar(
        Duration.ofSeconds(10) // Time is in seconds
);
```

