To start this microservices project, you can either run the services using **Docker Compose** or run each Spring Boot service **locally via Maven/terminal**.

---

### Option 1: Run with Docker Compose (Recommended)

This approach automatically spins up RabbitMQ (with the management dashboard) and builds all three microservices in separate containers.

1. **Build the JAR files** from the root directory:
```bash
mvn clean package -DskipTests

```


2. **Start all services**:
```bash
docker-compose up --build

```


3. **Verify running containers**:
* **RabbitMQ UI**: `http://localhost:15672` (Username: `admin`, Password: `password`)


* **Order Service**: `http://localhost:8081/api/orders/health`

* **Inventory Service**: `http://localhost:8082/api/inventory/health`

* **Notification Service**: `http://localhost:8083/api/notifications/health`




---

### Option 2: Run Locally (Step-by-Step)

If you prefer running and debugging each Spring Boot service directly on your machine:

#### 1. Start RabbitMQ

You can start just RabbitMQ using Docker:

```bash
docker run -d --name rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=admin \
  -e RABBITMQ_DEFAULT_PASS=password \
  rabbitmq:3-management

```

#### 2. Build the project

From the project root directory:

```bash
mvn clean install -DskipTests

```

#### 3. Start each microservice (in separate terminal windows)

* **Terminal 1 (`order-service` on port 8081)**:


```bash
cd order-service
mvn spring-boot:run

```


* **Terminal 2 (`inventory-service` on port 8082)**:


```bash
cd inventory-service
mvn spring-boot:run

```


* **Terminal 3 (`notification-service` on port 8083)**:


```bash
cd notification-service
mvn spring-boot:run

```



---

### Testing the Workflow

Once the services are running, test an end-to-end event flow:

1. **Create an Order**:
```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-001",
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "amount": 1200.00,
    "itemType": "LAPTOP",
    "quantity": 1
  }'

```


2. **Check Inventory Reduction**:
```bash
curl http://localhost:8082/api/inventory

```


3. **Check Sent Notifications**:
```bash
curl http://localhost:8083/api/notifications

```