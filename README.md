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

## Testing the Workflow

Once the services are running, test an end-to-end event flow:

#### 1. **Create an Order**:
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


#### 2. **Check Inventory Reduction**:
```bash
curl http://localhost:8082/api/inventory

```


#### 3. **Check Sent Notifications**:
```bash
curl http://localhost:8083/api/notifications

```

##  Project Work in Postman:
Запрос 1: Создание заказа (POST /api/orders)
Method (Метод): POST

URL: http://localhost:8081/api/orders


YML

Вкладка Headers:

Key: Content-Type

Value: application/json

Вкладка Body:

Выберите режим raw

В выпадающем списке справа выберите JSON

Вставьте тело запроса:

```JSON
{

  "customerId": "CUST-001",
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "productName": "LAPTOP",
  "price": 1200.00,
  "amount": 1200.00,
  "email": "john@example.com",
  "itemType": "LAPTOP",
  "quantity": 1
}
```
Нажмите кнопку Send. В ответе должен вернуться статус 201 Created и сгенерированный orderId.
##  Postman Answer:
```
POST http://localhost:8081/api/orders
201
369.46 ms
Network
Request Headers
Content-Type: application/json
Cache-Control: no-cache
Postman-Token: e5151ea6-9b78-4301-9e91-2f5a17dd4466
Content-Length: 156
Host: localhost:8081
User-Agent: PostmanRuntime/2.10.1
Accept: */*
Accept-Encoding: gzip, deflate, br
Connection: keep-alive
Request Body
Response Headers
content-type: application/json
transfer-encoding: chunked
date: Mon, 05 Oct 2026 18:07:38 GMT
keep-alive: timeout=60
connection: keep-alive
Response Body
{
  "orderId": "ORD-1791299940530",
  "customerId": "CUST-001",
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "amount": 1200.0,
  "status": "PENDING",
  "itemType": "LAPTOP",
  "quantity": 1,
  "createdAt": "2026-10-06T15:19:00.536430579",
  "message": "Order created"
}
```


JAVA

Запрос 2: Проверка остатков на складе (GET /api/inventory)
Method (Метод): GET

URL: http://localhost:8082/api/inventory


YML
Ещё 1

Вкладка Body: none (тело запроса не требуется)

Нажмите Send. В ответе отобразится текущее количество товаров (например, количество LAPTOP уменьшится с 50 до 49).   
JAVA
Ещё 1

Запрос 3: Проверка отправленных уведомлений (GET /api/notifications)
Method (Метод): GET

URL: http://localhost:8083/api/notifications


YML
Ещё 1

Вкладка Body: none (тело запроса не требуется)

Нажмите Send. В ответе отобразится список уведомлений с событием ORDER_CREATED для созданного заказа.   
JAVA
Ещё 1

Быстрый импорт через cURL в Postman
Чтобы не вводить параметры вручную:

В верхнем левом углу Postman нажмите кнопку Import.

Вставьте исходную команду 

curl -X POST http://localhost:8081/api/orders 

в поле ввода.

Postman автоматически заполнит метод, адрес, заголовки и JSON-тело.
```Json
{
  "ORD-1791219377567": {
    "orderId": "ORD-1791219377567",
    "customerId": null,
    "customerName": null,
    "customerEmail": null,
    "amount": 1200.0,
    "status": "PENDING",
    "itemType": null,
    "quantity": null,
    "createdAt": null,
    "message": null
  }
}
```

##  Docker commands for start project

```bash
docker-compose down
docker-compose up --build
```
