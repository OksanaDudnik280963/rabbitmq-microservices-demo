# RabbitMQ Spring Boot Microservices with Jenkins

A complete Java 17 / Spring Boot microservices example demonstrating RabbitMQ messaging, Cucumber BDD tests, Docker image builds, and Jenkins CI/CD automation.

## Table of Contents

1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [Technology Stack](#technology-stack)
4. [Project Structure](#project-structure)
5. [Prerequisites](#prerequisites)
6. [RabbitMQ Concepts Used](#rabbitmq-concepts-used)
7. [Services](#services)
8. [Run RabbitMQ](#run-rabbitmq)
9. [Run Locally](#run-locally)
10. [REST API Examples](#rest-api-examples)
11. [Cucumber BDD Tests](#cucumber-bdd-tests)
12. [Docker Images](#docker-images)
13. [Jenkins Installation](#jenkins-installation)
14. [Jenkins Configuration](#jenkins-configuration)
15. [Jenkins Pipeline](#jenkins-pipeline)
16. [Docker Registry](#docker-registry)
17. [Deployment](#deployment)
18. [Rollback](#rollback)
19. [Troubleshooting](#troubleshooting)
20. [Security Recommendations](#security-recommendations)

## Project Overview

This project demonstrates an event-driven microservices architecture using RabbitMQ as the message broker.

The main business flow is:

```text
Client
  |
  v
Order Service
  |
  | order.created / order.updated / order.cancelled
  v
RabbitMQ
  |
  +--> Inventory Service
  |
  +--> Notification Service
```

When a customer creates an order:

1. The Order Service accepts the REST request.
2. The Order Service publishes an `order.created` event.
3. RabbitMQ routes the event to subscribed queues.
4. The Inventory Service reserves stock.
5. The Notification Service sends a customer notification.

The services communicate asynchronously through RabbitMQ instead of calling each other directly.

## Architecture

### Services and ports

| Service | Port | Responsibility |
|---|---:|---|
| Order Service | 8081 | Creates, updates, and cancels orders; publishes events |
| Inventory Service | 8082 | Reserves and releases inventory |
| Notification Service | 8083 | Sends email, SMS, and push-style notifications |
| RabbitMQ | 5672 | AMQP messaging |
| RabbitMQ Management UI | 15672 | RabbitMQ administration |
| Jenkins | 8080 | CI/CD automation |

### Event flow

```text
POST /api/orders
       |
       v
Order Service
       |
       | convertAndSend("order.exchange", "order.created", order)
       v
Topic Exchange: order.exchange
       |
       +--> inventory.order.queue
       |       |
       |       +--> Reserve inventory
       |
       +--> notification.order.queue
       |       |
       |       +--> Send notification
       |
       +--> order.all.queue
               |
               +--> Audit and statistics
```

## Technology Stack

- Java 17.
- Spring Boot 3.x.
- Spring Web.
- Spring AMQP.
- RabbitMQ 3.x.
- Maven.
- Lombok.
- Docker.
- Docker Compose.
- Cucumber JVM.
- JUnit Platform.
- Testcontainers RabbitMQ.
- Awaitility.
- Jenkins Pipeline.
- Docker Registry.

## Project Structure

```text
rabbitmq-microservices-demo/
├── README.md
├── Jenkinsfile
├── pom.xml
├── docker-compose.yml
├── docker-compose.deploy.yml
│
├── docker/
│   ├── Dockerfile.order-service
│   ├── Dockerfile.inventory-service
│   └── Dockerfile.notification-service
│
├── ci/
│   ├── build-images.sh
│   ├── deploy.sh
│   └── health-check.sh
│
├── common-models/
│   ├── pom.xml
│   └── src/main/java/com/example/commonmodels/
│       ├── Order.java
│       ├── Notification.java
│       └── InventoryEvent.java
│
├── order-service/
│   ├── pom.xml
│   ├── src/main/java/com/example/orderservice/
│   │   ├── OrderServiceApplication.java
│   │   ├── config/RabbitMQConfig.java
│   │   ├── controller/OrderController.java
│   │   ├── consumer/OrderConsumer.java
│   │   ├── producer/OrderProducer.java
│   │   ├── service/OrderService.java
│   │   └── model/
│   │       ├── OrderRequest.java
│   │       └── OrderResponse.java
│   ├── src/main/resources/application.yml
│   └── src/test/
│       ├── java/com/example/orderservice/bdd/
│       │   ├── CucumberSpringConfiguration.java
│       │   ├── RunCucumberTest.java
│       │   ├── config/RabbitMqTestConfig.java
│       │   └── steps/OrderStepDefinitions.java
│       └── resources/
│           ├── application-test.yml
│           └── features/order_processing.feature
│
├── inventory-service/
│   ├── pom.xml
│   ├── src/main/java/com/example/inventoryservice/
│   │   ├── InventoryServiceApplication.java
│   │   ├── config/RabbitMQConfig.java
│   │   ├── controller/InventoryController.java
│   │   ├── consumer/InventoryConsumer.java
│   │   └── service/InventoryService.java
│   └── src/main/resources/application.yml
│
└── notification-service/
    ├── pom.xml
    ├── src/main/java/com/example/notificationservice/
    │   ├── NotificationServiceApplication.java
    │   ├── config/RabbitMQConfig.java
    │   ├── controller/NotificationController.java
    │   ├── consumer/NotificationConsumer.java
    │   └── service/NotificationService.java
    └── src/main/resources/application.yml
```

## Prerequisites

Install the following software:

```text
Java 17
Maven 3.9+
Docker Desktop
Git
Jenkins
```

Check your installation:

```bash
java -version
mvn -version
docker version
git --version
```

Docker must be running for:

- RabbitMQ containers.
- Testcontainers BDD tests.
- Docker image builds.
- Jenkins Docker stages.

## RabbitMQ Concepts Used

### Exchange

An exchange receives messages and routes them to queues.

The project uses:

```text
order.exchange       Topic exchange
inventory.exchange   Topic exchange
```

### Queue

A queue stores messages until a consumer processes them.

Examples:

```text
order.created.queue
order.updated.queue
order.cancelled.queue
order.all.queue
inventory.order.queue
notification.order.queue
```

### Routing key

Routing keys determine where a message goes.

Examples:

```text
order.created
order.updated
order.cancelled
```

### Topic wildcards

The `*` wildcard matches one word:

```text
order.*
```

It matches:

```text
order.created
order.updated
order.cancelled
```

The `#` wildcard matches zero or more words:

```text
order.#
```

## Services

### Order Service

Base URL:

```text
http://localhost:8081
```

Responsibilities:

- Accept order REST requests.
- Create an order.
- Publish RabbitMQ events.
- Process order events.
- Provide health and query endpoints.

Main RabbitMQ producer call:

```java
rabbitTemplate.convertAndSend(
        "order.exchange",
        "order.created",
        order
);
```

### Inventory Service

Base URL:

```text
http://localhost:8082
```

Responsibilities:

- Listen to order events.
- Reserve stock for new orders.
- Release stock for cancelled orders.
- Publish inventory events.

Default inventory:

```text
LAPTOP: 50
PHONE: 100
TABLET: 75
HEADPHONES: 200
```

### Notification Service

Base URL:

```text
http://localhost:8083
```

Responsibilities:

- Listen to order events.
- Create email notifications.
- Create SMS notifications.
- Record notification statistics.

The example simulates notification delivery using log messages. A production implementation would connect to an email, SMS, or push provider.

## Run RabbitMQ

### Docker command

```bash
docker run -d \
  --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=admin \
  -e RABBITMQ_DEFAULT_PASS=password \
  rabbitmq:3-management
```

Windows PowerShell:

```powershell
docker run -d `
  --name rabbitmq `
  -p 5672:5672 `
  -p 15672:15672 `
  -e RABBITMQ_DEFAULT_USER=admin `
  -e RABBITMQ_DEFAULT_PASS=password `
  rabbitmq:3-management
```

RabbitMQ Management UI:

```text
http://localhost:15672
```

Credentials:

```text
Username: admin
Password: password
```

### Docker Compose

Start RabbitMQ only:

```bash
docker compose up -d rabbitmq
```

Check status:

```bash
docker compose ps
```

View logs:

```bash
docker compose logs -f rabbitmq
```

Stop RabbitMQ:

```bash
docker compose down -v
```

## Run Locally

### Build all Maven modules

From the project root:

```bash
mvn clean install
```

Skip tests:

```bash
mvn clean install -DskipTests
```

### Start the Order Service

```bash
cd order-service
mvn spring-boot:run
```

### Start the Inventory Service

Open another terminal:

```bash
cd inventory-service
mvn spring-boot:run
```

### Start the Notification Service

Open another terminal:

```bash
cd notification-service
mvn spring-boot:run
```

### Run all services with Docker Compose

First build the JAR files:

```bash
mvn clean package -DskipTests
```

Then build and start all containers:

```bash
docker compose up --build
```

Run in the background:

```bash
docker compose up --build -d
```

Stop all services:

```bash
docker compose down -v
```

## REST API Examples

### Create an order

```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-001",
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "amount": 999.99,
    "itemType": "LAPTOP",
    "quantity": 2
  }'
```

PowerShell:

```powershell
$body = @{
    customerId = "CUST-001"
    customerName = "John Doe"
    customerEmail = "john@example.com"
    amount = 999.99
    itemType = "LAPTOP"
    quantity = 2
} | ConvertTo-Json

Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8081/api/orders `
  -ContentType "application/json" `
  -Body $body
```

### Create an order with publisher confirmation

```bash
curl -X POST http://localhost:8081/api/orders/confirmed \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-002",
    "customerName": "Jane Doe",
    "customerEmail": "jane@example.com",
    "amount": 499.99,
    "itemType": "PHONE",
    "quantity": 1
  }'
```

### Get all orders

```bash
curl http://localhost:8081/api/orders
```

### Get one order

```bash
curl http://localhost:8081/api/orders/{orderId}
```

### Cancel an order

```bash
curl -X DELETE http://localhost:8081/api/orders/{orderId}
```

### Get inventory

```bash
curl http://localhost:8082/api/inventory
```

### Check inventory availability

```bash
curl "http://localhost:8082/api/inventory/check?itemType=LAPTOP&quantity=5"
```

### Get notification history

```bash
curl http://localhost:8083/api/notifications
```

### Send a test email notification

```bash
curl -X POST \
  "http://localhost:8083/api/notifications/test/email?email=test@example.com&message=Hello"
```

### Health endpoints

```bash
curl http://localhost:8081/api/orders/health
curl http://localhost:8082/api/inventory/health
curl http://localhost:8083/api/notifications/health
```

## Cucumber BDD Tests

The project uses:

- `cucumber-java`.
- `cucumber-spring`.
- `cucumber-junit-platform-engine`.
- JUnit Platform Suite.
- Testcontainers RabbitMQ.
- Awaitility.

### Feature file

```text
order-service/src/test/resources/features/order_processing.feature
```

Example:

```gherkin
Feature: Order processing through RabbitMQ

  Scenario: Create an order and publish an order-created event
    Given RabbitMQ is running
    When I create an order for customer "CUST-100" with email "customer@example.com" for 2 "LAPTOP" items costing 1999.98
    Then the order response status should be 201
    And an "order.created" message should be published
    And the message should contain customer "CUST-100"
```

### Run tests

Run all tests:

```bash
mvn clean test
```

Run only the Order Service tests:

```bash
mvn -pl order-service -am test
```

Run the Cucumber runner:

```bash
mvn -pl order-service -am \
  -Dtest=RunCucumberTest \
  test
```

Docker must be running because Testcontainers starts RabbitMQ automatically during the test.

### Test reports

Cucumber reports are generated here:

```text
order-service/target/cucumber-report.html
order-service/target/cucumber.json
order-service/target/cucumber.xml
```

JUnit reports are generated here:

```text
order-service/target/surefire-reports/
```

## Docker Images

### Build JAR files

```bash
mvn clean package -DskipTests
```

### Build Order Service image

```bash
docker build \
  -f docker/Dockerfile.order-service \
  -t rabbitmq/order-service:local \
  .
```

### Build Inventory Service image

```bash
docker build \
  -f docker/Dockerfile.inventory-service \
  -t rabbitmq/inventory-service:local \
  .
```

### Build Notification Service image

```bash
docker build \
  -f docker/Dockerfile.notification-service \
  -t rabbitmq/notification-service:local \
  .
```

### List images

```bash
docker image ls | grep rabbitmq
```

### Run the images

```bash
export DOCKER_NAMESPACE=rabbitmq
export IMAGE_TAG=local

docker compose up -d
```

PowerShell:

```powershell
$env:DOCKER_NAMESPACE = "rabbitmq"
$env:IMAGE_TAG = "local"

docker compose up -d
```

## Jenkins Installation

### Start Jenkins with Docker

Create Jenkins data storage:

```powershell
mkdir C:\jenkins_home
```

Create a Docker network:

```powershell
docker network create jenkins
```

Start Jenkins:

```powershell
docker run -d `
  --name jenkins `
  --restart unless-stopped `
  --network jenkins `
  -p 8080:8080 `
  -p 50000:50000 `
  -v C:\jenkins_home:/var/jenkins_home `
  -v //var/run/docker.sock:/var/run/docker.sock `
  jenkins/jenkins:lts-jdk21
```

Open:

```text
http://localhost:8080
```

Get the initial password:

```powershell
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Select **Install suggested plugins**, then create an administrator account.

### Jenkins plugins

Install:

```text
Pipeline
Git
GitHub Integration
Maven Integration
JUnit
Docker Pipeline
Docker Commons
Docker Workflow
Credentials Binding
SSH Agent
Cucumber Reports
HTML Publisher
Timestamper
```

## Jenkins Configuration

### Docker Hub credentials

Create a Jenkins credential:

```text
Kind: Username with password
ID: docker-hub-credentials
Username: your Docker Hub username
Password: Docker Hub access token
```

### Deployment SSH credentials

Create another credential:

```text
Kind: SSH Username with private key
ID: deploy-server-ssh
Username: deploy
Private key: deployment private key
```

Do not put passwords or private keys in the repository.

### Create a Pipeline job

1. Select **New Item**.
2. Enter `rabbitmq-microservices-demo`.
3. Select **Pipeline**.
4. Select **Pipeline script from SCM**.
5. Select **Git**.
6. Enter the repository URL.
7. Select the branch.
8. Set the script path to `Jenkinsfile`.
9. Save the job.
10. Click **Build Now**.

## Jenkins Pipeline

The root `Jenkinsfile` should perform these stages:

```text
Checkout
Verify tools
Compile
Unit and BDD tests
Publish test reports
Package JAR files
Build Docker images
Run RabbitMQ smoke test
Push Docker images
Deploy to staging
Verify staging deployment
Approve production deployment
Deploy to production
```

### Jenkinsfile environment values

Update these values for your project:

```groovy
environment {
    DOCKER_NAMESPACE = 'your-docker-user'

    ORDER_IMAGE = "${DOCKER_NAMESPACE}/order-service"
    INVENTORY_IMAGE = "${DOCKER_NAMESPACE}/inventory-service"
    NOTIFICATION_IMAGE = "${DOCKER_NAMESPACE}/notification-service"

    DOCKER_CREDENTIALS_ID = 'docker-hub-credentials'
    DEPLOY_SSH_CREDENTIALS_ID = 'deploy-server-ssh'

    DEPLOY_SERVER = 'your.server.example.com'
    DEPLOY_USER = 'deploy'
    DEPLOY_DIRECTORY = '/opt/rabbitmq-microservices'
}
```

### Pipeline commands

Compile:

```bash
mvn -B -DskipTests clean compile
```

Test:

```bash
mvn -B test
```

Package:

```bash
mvn -B -DskipTests package
```

Build Docker image:

```bash
docker build \
  -f docker/Dockerfile.order-service \
  -t your-docker-user/order-service:${IMAGE_TAG} \
  .
```

Push Docker image:

```groovy
docker.withRegistry(
    'https://index.docker.io/v1/',
    'docker-hub-credentials'
) {
    sh 'docker push your-docker-user/order-service:${IMAGE_TAG}'
}
```

## Docker Registry

Login locally:

```bash
docker login
```

Tag an image:

```bash
docker tag \
  rabbitmq/order-service:local \
  your-docker-user/order-service:1
```

Push an image:

```bash
docker push your-docker-user/order-service:1
```

Recommended image tags:

```text
25-a13f9bc
26-c82b1ef
27-3f4a191
```

Avoid deploying only `latest`, because versioned tags support traceability and rollback.

## Deployment

### Deployment server prerequisites

The target Linux server needs:

```text
Docker
Docker Compose plugin
SSH server
Access to the Docker registry
```

Create a deployment directory:

```bash
sudo mkdir -p /opt/rabbitmq-microservices
sudo chown -R deploy:deploy /opt/rabbitmq-microservices
```

### Deployment Compose file

Create `docker-compose.deploy.yml` in the project root and copy it to the server through Jenkins.

The deployment server receives the selected image tag:

```bash
export DOCKER_NAMESPACE=your-docker-user
export IMAGE_TAG=25-a13f9bc

docker compose pull
docker compose up -d --remove-orphans
```

### Deployment health checks

After deployment, Jenkins should call:

```bash
curl --fail http://localhost:8081/api/orders/health
curl --fail http://localhost:8082/api/inventory/health
curl --fail http://localhost:8083/api/notifications/health
```

If a health endpoint fails, Jenkins should mark the deployment as failed.

## Rollback

Rollback by deploying a previous image tag:

```bash
export DOCKER_NAMESPACE=your-docker-user
export IMAGE_TAG=24-7a2cd10

docker compose pull
docker compose up -d --remove-orphans
```

Use a Jenkins string parameter called `ROLLBACK_TAG`:

```groovy
parameters {
    string(
        name: 'ROLLBACK_TAG',
        defaultValue: '',
        description: 'Docker tag for rollback'
    )
}
```

## Troubleshooting

### RabbitMQ connection refused

Check RabbitMQ:

```bash
docker ps
docker logs rabbitmq
```

Check the management UI:

```text
http://localhost:15672
```

Verify application configuration:

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: password
```

Inside Docker Compose, the host must be the service name:

```yaml
RABBITMQ_HOST: rabbitmq
```

### Dockerfile cannot find JAR

Build the JAR first:

```bash
mvn clean package -DskipTests
```

Run Docker build from the project root:

```bash
docker build -f docker/Dockerfile.order-service .
```

Check that the Dockerfile uses:

```dockerfile
COPY order-service/target/order-service-1.0.0.jar app.jar
```

### Cucumber test not found

Check:

```text
src/test/resources/features/*.feature
```

Check the runner:

```java
@SelectClasspathResource("features")
```

Check the glue package:

```java
@ConfigurationParameter(
    key = GLUE_PROPERTY_NAME,
    value = "com.example.orderservice.bdd"
)
```

### Testcontainers cannot start

Check Docker:

```bash
docker version
```

The Jenkins agent and local test process must have access to the Docker daemon.

### Jenkins cannot run Docker

On Linux, add Jenkins to the Docker group:

```bash
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

Check the Jenkins agent:

```bash
docker version
```

### Jenkins cannot authenticate to Docker Hub

Check:

```text
Credential ID: docker-hub-credentials
Credential type: Username with password
Password: Docker access token
```

The ID in the Jenkinsfile must match the credential ID.

### Jenkins cannot connect through SSH

Check:

```text
Credential ID: deploy-server-ssh
Remote username
Remote hostname
SSH public key in ~/.ssh/authorized_keys
```

Test manually from the Jenkins agent:

```bash
ssh deploy@your.server.example.com
```

## Security Recommendations

For development, the example uses:

```text
RabbitMQ username: admin
RabbitMQ password: password
```

For production:

- Use strong RabbitMQ credentials.
- Store credentials in Jenkins Credentials.
- Use TLS for RabbitMQ.
- Do not expose RabbitMQ port 5672 publicly.
- Restrict RabbitMQ Management UI access.
- Use private Docker registry credentials.
- Use SSH keys rather than passwords.
- Do not commit `.env` files with secrets.
- Use separate credentials for staging and production.
- Require manual approval before production deployment.
- Use immutable Docker image tags.
- Keep RabbitMQ queues durable.
- Implement dead-letter queues.
- Make consumers idempotent.
- Monitor queue depth and consumer failures.

## Useful Commands

Build project:

```bash
mvn clean install
```

Run tests:

```bash
mvn test
```

Build images:

```bash
docker compose build
```

Start services:

```bash
docker compose up -d
```

View service logs:

```bash
docker compose logs -f order-service
docker compose logs -f inventory-service
docker compose logs -f notification-service
```

List containers:

```bash
docker compose ps
```

Stop everything:

```bash
docker compose down -v
```

Remove unused images:

```bash
docker image prune -f
```

## CI/CD Summary

The complete process is:

```text
Developer pushes code
        |
        v
Jenkins checks out source
        |
        v
Maven compiles project
        |
        v
Unit and Cucumber tests run
        |
        v
JUnit and Cucumber reports published
        |
        v
Spring Boot JAR files packaged
        |
        v
Docker images built
        |
        v
Images pushed to Docker Registry
        |
        v
Deployment server pulls images
        |
        v
Docker Compose starts services
        |
        v
Health checks verify deployment
```

## License

This project is intended for learning and demonstration purposes. Adapt security, persistence, monitoring, and deployment configuration before using it in production.
