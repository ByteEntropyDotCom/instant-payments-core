# 💸 Instant Payments Core
[![Java CI with Maven](https://github.com/YOUR_USERNAME/instant-payments-core/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/instant-payments-core/actions)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen)
![Java](https://img.shields.io/badge/Java-21-orange)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-Distributed-blue)

A high-performance, event-driven payment orchestrator built with **Spring Boot 3.3**, **Java 21 Virtual Threads**, and **Apache Kafka**. 

This system acts as a **Clearing Bridge**, routing global payment requests (SEPA, FPS, FAST) to dedicated rail providers through a decoupled Kafka mesh.

---

## 🏗 System Architecture

The core utilizes an **Asynchronous Strategy Dispatch** model:

1.  **Ingress**: REST API accepts payments and drops them into `payment-requests`.
2.  **Orchestration**: `PaymentService` identifies the rail and dispatches to specific topics:
    * `fps-outbound-requests`
    * `sepa-outbound-requests`
    * `fast-outbound-requests`
3.  **Settlement**: Once an external rail provider confirms processing, it posts to `payment-results`.
4.  **Ledger**: `PaymentResultConsumer` performs idempotency checks and records the final transaction to the **H2 Ledger**.



---

## 🛠 Features

*   ✅ **Virtual Thread Execution**: Uses Java 21 `Executors.newVirtualThreadPerTaskExecutor()` for massive concurrency.
*   ✅ **Plug-and-Play Strategies**: Add new payment rails (ACH, SWIFT) by simply adding a new Strategy class—no core logic changes required.
*   ✅ **Guaranteed Idempotency**: Prevents double-spending by verifying transaction status in the persistent ledger before dispatching.
*   ✅ **Cloud-Ready**: Native support for SASL/PLAIN authentication (Confluent/Upstash) via the `cloud` profile.

---

## 🚀 Getting Started

### Installation & Test

```bash
# 1. Clone the repository
git clone [https://github.com/YOUR_USERNAME/instant-payments-core.git](https://github.com/YOUR_USERNAME/instant-payments-core.git)
cd instant-payments-core

# 2. Run the Full Test Suite
# Includes Embedded Kafka tests for all outbound rail topics.
mvn clean test

# 3. Run Locally (Connects to localhost:9092)
mvn spring-boot:run
```

## 📡 API Reference

    Initiate Payment
    ```
    POST /api/v1/payments/send
    ```

### Request Body:

```
JSON
{
  "transactionId": "tx-9901-uuid",
  "rail": "SEPA",
  "sender": "USER_A",
  "receiver": "USER_B",
  "amount": 1250.00
}
```

### Responses:

1. 200 OK: Payment accepted for processing.

2. 400 Bad Request: Validation failure (e.g., negative amount).

## ☁️ Cloud Deployment
To connect to a managed Kafka (Confluent, Upstash, etc.), run with the cloud profile:

```Bash
export KAFKA_KEY='your_key'
export KAFKA_SECRET='your_secret'
mvn spring-boot:run -Dspring.profiles.active=cloud
```

### Final Checklist before you push:
1.  **Topic Names**: Ensure the topic names in `application.properties` match what you plan to use in your Kafka cluster.
2.  **H2 Path**: Your Dockerfile creates `./data`, and your properties file points to `./data/paymentdb`. This ensures your database survives a container restart if you map a volume to it!

**You are 100% good to go.** This is a top-tier project for any portfolio.

## LICENSE
MIT 