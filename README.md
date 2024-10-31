![Java Version](https://img.shields.io/badge/Java-v23-red)
![Repository Size](https://img.shields.io/github/repo-size/dzenthai/Cryptora-Analyze-Service?color=red)

<div style="display: flex; flex-wrap: wrap; justify-content: center;">
    <img src="./assets/Binance-logo.png" width="65" height="65" style="margin: 10px;" alt="">
    <img src="./assets/Intellij-logo.png" width="65" height="65" style="margin: 10px;" alt="">
</div>

# Cryptora-Analyze-Service

## **Description**

**Cryptora-Analyze-Service** is a Spring Boot application that performs real-time cryptocurrency market analysis using
the Binance API and RabbitMQ. The service tracks price movements, analyzes short- and long-term trends, and generates
trading signals (buy, sell, hold), which are sent through RabbitMQ.

---

## **Key Features**

- **Real-time Data Fetching**: The application integrates with the Binance API to retrieve live cryptocurrency data.
  Users can track any available cryptocurrencies.

- **Cryptocurrency History Storage**: MongoDB is used to store the history of cryptocurrency prices, allowing users to
  analyze historical growth and decline trends.

- **Price Trend Analysis**: With stored price data, users can perform trend analysis on cryptocurrency growth and
  decline, supported by the Ta4j library.

- **Trading Signal Generation and Delivery**: Generated signals are sent via a RabbitMQ message queue, making it easy to
  connect and deliver this data to other services or end users.

---

## **Technologies**

- **Java**: The primary programming language.

- **Spring Boot**: Framework used for building the service.

- **MongoDB**: A NoSQL database used for storing real-time cryptocurrency quotes.

- **Binance API**: External API for retrieving up-to-date cryptocurrency price information in real-time.

- **Ta4j**: A library used for analyzing price movements, including growth and decline of cryptocurrencies.

- **RabbitMQ**: Message queue for asynchronous delivery of trading signals, enabling integration with other systems.

- **Docker**: Containerization platform that helps package the application with its dependencies, ensuring consistent
  environments and simplifying deployment.

---

## **How it Works**

### **Cryptocurrency Analysis**

The application, deployed in Docker, performs cryptocurrency analysis and provides recommendations, such as "Hold" or "
Sell". The logs show the real-time analysis results for each cryptocurrency.

<img src="./assets/Docker-example.png" alt="">

In the example above:

The analytic service starts the analysis and determines that for ETH, BTC, and TON, the recommended action is to hold (
HOLD).

### **RabbitMQ Message Queue**

After analysis, recommendations are sent to a RabbitMQ queue named CryptoraQueue. RabbitMQ is used to manage messages,
with each message representing a recommendation for a specific cryptocurrency. These messages can be processed by other
services subscribed to the queue.

<img src="./assets/Rabbitmq-example.png" alt="">

The image above shows a queue with multiple messages:

Message 1: Recommendation to Hold ETH.
Message 2: Recommendation to Hold BTC.
Message 3: Recommendation to Sell TON.
Each message contains additional attributes, such as priority, delivery_mode, and content_type, which allow the system
to handle them flexibly and provide recommendations in real time.

---

## **Installation Guide**

### **Prerequisites**

- Java 23
- Gradle 8.10.2
- Docker 27.2.0

### **Installation and Startup Steps**

1. **Clone the Repository**
   ```bash
   git clone https://github.com/dzenthai/Cryptora-Analyze-Service.git
   cd Cryptora-Analyze-Service
   ```

2. **Add Environment Variables**
   Create an .env file and add the required environment variables such as the Binance API key and secret.

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

To add new cryptocurrency pairs, update the `Ticker` class by adding them to the Ticker enum:

```java
enum Ticker {
    BTC,
    ETH,
    TON,
    // add a new pair here
}
```

### **Adjusting Data Fetching Interval**

To modify the interval at which the application fetches and analyzes price data, update the following line in
the `AppScheduler` class:

```java

@Scheduled(fixedRate = 10000) // Time is in milliseconds
public void executeInSequence() {
}
```

You can adjust the short-term and long-term periods for the moving average in `AnalyticService` using parameters in the
application.yaml file:

```yaml
cryptora:
  short:
    time:
      period: 50
  long:
    time:
      period: 200
```

Also, it is recommended to adjust the duration in the `AnalyticService` class to match, as shown below:

```java
var bar = new BaseBar(
        Duration.ofHours(1)
        // Other settings...
);
```
