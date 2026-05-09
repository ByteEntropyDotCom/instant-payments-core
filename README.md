# 💸 Instant Payments Core
[![Java CI with Maven](https://github.com/YOUR_USERNAME/instant-payments-core/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/instant-payments-core/actions)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-Distributed-blue)

A robust, event-driven payment processing engine designed for high-concurrency financial transactions. Built with **Spring Boot**, **Apache Kafka**, and **H2**.

---

## 🏗 System Architecture

The core follows a decoupled, asynchronous event-chain to ensure high availability and data integrity.



1.  **Ingress**: REST Controller validates and accepts the payment request.
2.  **Messaging**: `payment-requests` topic handles the command.
3.  **Processing**: `PaymentRequestConsumer` executes business logic via `PaymentService`.
4.  **Result**: `payment-results` topic broadcasts success/failure.
5.  **Persistence**: `PaymentResultConsumer` ensures the ledger is updated in **H2 Database**.

---

## 🛠 Features

*   ✅ **Idempotency**: Built-in protection against double-spending and duplicate Kafka messages.
*   ✅ **Schema Validation**: Strict validation for amounts (positive only) and required fields.
*   ✅ **Multi-Profile Support**: Seamlessly switch between `local` (Dev) and `cloud` (Prod) environments.
*   ✅ **Self-Contained Testing**: 100% test coverage using **Embedded Kafka** (no external setup required to run tests).

---

## 🚀 Getting Started

### Prerequisites
*   **Java 21** or higher
*   **Maven 3.9+**
*   **Docker** (Optional, for local Kafka)

### Installation & Test

```
# 1. Clone the repository
git clone https://github.com/YOUR_USERNAME/instant-payments-core.git
cd instant-payments-core

# 2. Run the Integration Test Suite
# This verifies the Happy Path, Idempotency, and Validation logic using Embedded Kafka.
mvn clean test

# 3. Start a local Kafka Broker (Required for manual local run)
# Skip this step if you are only running tests.
docker run -d --name kafka-local -p 9092:9092 apache/kafka:latest

# 4. Run the Application locally
# The app will connect to localhost:9092 by default.
mvn spring-boot:run

# 5. (Alternative) Run with Cloud Profile
# Use this if connecting to a managed service like Confluent or Upstash.
export KAFKA_KEY='your_api_key'
export KAFKA_SECRET='your_api_secret'
mvn spring-boot:run -Dspring.profiles.active=cloud
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
  "transactionId": "unique-uuid-12345",
  "rail": "FPS",
  "sender": "ACC_001",
  "receiver": "ACC_999",
  "amount": 500.25
}
```

### Responses:

1. 200 OK: Payment accepted for processing.

2. 400 Bad Request: Validation failure (e.g., negative amount).

3. 500 Internal Error: Infrastructure or unexpected failure.

## ☁️ Cloud Deployment
To connect to a managed Kafka (Confluent, Upstash, etc.), run with the cloud profile:

```Bash
export KAFKA_KEY='your_key'
export KAFKA_SECRET='your_secret'
mvn spring-boot:run -Dspring.profiles.active=cloud
```
