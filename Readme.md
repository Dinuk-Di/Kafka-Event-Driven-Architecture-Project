# Event-Driven Architecture with Spring Boot and Kafka

![Java](https://img.shields.io/badge/Java-21-blue.svg) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg) ![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-darkblue.svg) ![Docker](https://img.shields.io/badge/Docker%20Compose-blue.svg)

This project demonstrates a real-world, event-driven microservices architecture using **Spring Boot (Java 21)**, **Apache Kafka**, and **Docker Compose**. The system simulates an e-commerce order flow where services communicate asynchronously and in a decoupled fashion.

When an order is placed, an `OrderEvent` is published to a Kafka topic. This single event is then consumed by multiple downstream services concurrently to handle tasks like updating stock and sending notifications.

---

## 🏛️ Architecture Overview

The architecture is centered around a Kafka message broker which acts as the "single source of truth" for events.

* **Producer (`order-service`):** A REST API that accepts new orders. When an order is created, it publishes a message to the `order-topic`.
* **Consumers (`stock-service`, `email-service`):** These services subscribe to the `order-topic`. They run independently and in parallel.
    * `stock-service` consumes the event to update the product stock.
    * `email-service` consumes the event to send a confirmation email to the user.
* **Shared Module (`base-domains`):** A common Java module that contains the Data Transfer Objects (DTOs), such as the `OrderEvent` class, ensuring all services speak the same language.

### Diagram: The Event Flow

```mermaid
graph TD;
    subgraph User
        UserClient[<br>User / Postman<br>]
    end

    subgraph Services
        OS[<b>order-service</b><br>Port: 8085<br>POST /api/v1/orders]
        SS[<b>stock-service</b><br>Port: 8089<br>Consumer]
        ES[<b>email-service</b><br>Port: 8083<br>Consumer]
    end

    subgraph Kafka Cluster
        Kafka(<b>kafka-broker</b><br>order-topic)
    end
    
    subgraph External
        EmailAPI[SendGrid API]
    end

    subgraph Debugging
        DebugConsumer[<b>kafka-consumer</b><br>CLI Tool]
    end

    UserClient --
    1. HTTP POST --> OS;
    OS -- 2. Publishes OrderEvent --> Kafka;
    Kafka -- 3a. Consumes Event --> SS;
    Kafka -- 3b. Consumes Event --> ES;
    Kafka -- 3c. Consumes Event --> DebugConsumer;
    ES -- 4. Sends Email --> EmailAPI;
````

-----

## 🛠️ Services

| Service | Port | Description |
| :--- | :--- | :--- |
| **`order-service`** | 8085 | REST API for creating new orders. Publishes events to Kafka. |
| **`stock-service`** | 8089 | Subscribes to Kafka to update stock levels based on new orders. |
| **`email-service`** | 8083 | Subscribes to Kafka to send order confirmation emails. |
| **`kafka`** | 9092 | The Kafka message broker service. |
| **`zookeeper`** | 2181 | Required by Kafka for cluster management. |
| **`kafka-consumer`** | N/A | A simple CLI tool that prints all messages from `order-topic` to the log. |

-----

## 🚀 Getting Started

Follow these instructions to build and run the entire application using Docker.

### Prerequisites

  * **Docker Desktop** (or Docker Engine + Docker Compose) installed.
  * **SendGrid API Key**: The `email-service` is configured to use SendGrid. You will need a valid API key.

### 1\. Configuration

Before running the application, you must provide your SendGrid API key and a verified sender email.

Open the `docker-compose.yml` file and find the `email-service` section. Update these two environment variables with your real credentials:

```yaml
  email-service:
    # ... (other config) ...
    environment:
      # ...
      # Placeholders for your API keys
      SENDGRID_API_KEY: "YOUR_REAL_SENDGRID_API_KEY"  # <-- PUT YOUR KEY HERE
      SENDGRID_FROM_EMAIL: "your-verified-sender@example.com" # <-- PUT YOUR EMAIL HERE
```

### 2\. Build and Run

From the root directory of the project (where your `docker-compose.yml` is located), run the following command:

```bash
docker-compose up --build -d
```

  * `--build`: This forces Docker to rebuild your Java applications from their Dockerfiles.
  * `-d`: Runs the containers in detached mode (in the background).

It may take a few minutes for all the services to build and start up.

### 3\. Stopping the Application

To stop and remove all running containers, run:

```bash
docker-compose down
```

-----

## ✅ Verifying the System

You can test the entire flow by posting a new order.

### Step 1: Send an Order

Use `curl` or a tool like Postman to send a `POST` request to the `order-service`.

```bash
# Example curl command for your terminal
curl -X POST 'http://localhost:8085/api/v1/orders' \
-H 'Content-Type: application/json' \
-d '{
    "orderId": "1001",
    "name": "MacBook Pro",
    "qty": 1,
    "price": 2499.99
}'
```

The `order-service` should return a message like `Order placed successfully!`.

### Step 2: Watch the Logs

This is the best part. Open three new terminal windows.

1.  **In Terminal 1 (Watch the Kafka Consumer):**
    See the raw event appear in the `kafka-consumer` log.

    ```bash
    docker logs -f kafka-consumer
    ```

    **Output:**

    ```
    Kafka is ready!
    {"orderId":"1001","name":"MacBook Pro","qty":1,"price":2499.99}
    ```

2.  **In Terminal 2 (Watch the Stock Service):**
    See the `stock-service` log that it received and processed the event.

    ```bash
    docker logs -f stock-service
    ```

    **Output:**

    ```
    2025-11-11T14:30:00.123 INFO 1 --- [stock-service] ... : Order event received => {"orderId":"1001", ...}
    2025-11-11T14:30:00.124 INFO 1 --- [stock-service] ... : Stock updated for order 1001
    ```

3.  **In Terminal 3 (Watch the Email Service):**
    See the `email-service` log that it received the event and sent an email.

    ```bash
    docker logs -f email-service
    ```

    **Output:**

    ```
    2025-11-11T14:30:00.125 INFO 1 --- [email-service] ... : Order event received => {"orderId":"1001", ...}
    2025-11-11T14:30:00.567 INFO 1 --- [email-service] ... : Email sent successfully!
    ```