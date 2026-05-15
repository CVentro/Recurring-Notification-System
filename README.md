# Recurring Notification System

A microservices-based notification platform built with Java, Spring Boot, Kafka, MongoDB, AWS AppConfig, AWS Secrets Manager, Docker, and observability tooling.

The system supports fixed, recurring, and fixed-recurring notifications, publishes scheduled events through Kafka, processes delivery in a dedicated notification service, and handles retry/DLQ flows for failures.

## Architecture

```text
Client
  |
  v
Gateway Server
  |
  v
Recurring Service  ---> MongoDB
  |
  v
Kafka Topics
  |
  v
Notification Service ---> AWS Secrets Manager
  |
  v
Email/SMS Sender

Eureka Server provides service discovery.
Infa Service creates/administers Kafka topics.
Prometheus, Grafana, Filebeat, Elasticsearch, and Kibana provide observability.
```

## Services

| Service | Port | Responsibility |
| --- | ---: | --- |
| `eurekaServer` | `8761` | Service discovery |
| `gatewayServer` | `8765` | API gateway and routing |
| `infaService` | `8010` | Kafka admin/topic setup |
| `recurringService` | `8001` | Create, store, schedule, and publish notifications |
| `notificationService` | `8002` | Consume Kafka messages, send notifications, retry failures |

## Tech Stack

- Java 17
- Spring Boot
- Spring Cloud Netflix Eureka
- Spring Cloud Gateway
- Apache Kafka
- MongoDB
- AWS AppConfig
- AWS Secrets Manager
- Docker Compose
- Prometheus
- Grafana
- Elasticsearch
- Kibana
- Filebeat
- JUnit, Mockito
- GitHub Actions

## Notification Flow

1. A notification event is created through `recurringService`.
2. The event is stored in MongoDB.
3. The scheduler finds eligible events and publishes them to Kafka.
4. `notificationService` consumes the Kafka message.
5. The sender sends the notification.
6. On success, the notification status/count is updated.
7. On failure, retry metadata is updated and the message is moved through retry topics.
8. If retries are exhausted, the notification is cancelled or routed to DLQ behavior.

## Kafka Topics

`infaService` creates and manages these topics:

- `notifications.email.main`
- `notifications.email.retry.1m`
- `notifications.email.retry.5m`
- `notifications.email.dlq`

## Running Locally From IntelliJ

Use the `local-test` Compose file to run only infrastructure, then start the Spring Boot services from IntelliJ.

```bash
docker compose -f local-test/docker-compose.yml up -d
```

This starts:

- MongoDB: `localhost:27017`
- Kafka: `localhost:9092`
- Elasticsearch: `localhost:9200`
- Kibana: `localhost:5601`
- Prometheus: `localhost:9090`
- Grafana: `localhost:3000`
- Filebeat reading logs from `./logs`

Then run services from IntelliJ in this order:

1. `eurekaServer`
2. `infaService`
3. `recurringService`
4. `notificationService`
5. `gatewayServer`

Default app properties are already configured for local IntelliJ usage with `localhost` dependencies.

## Running Full Stack Locally With Docker

Run all services and infrastructure in Docker:

```bash
APP_ENV=DEV docker compose up -d --build
```

Useful URLs:

- Eureka: `http://localhost:8761`
- Gateway: `http://localhost:8765`
- Recurring Service: `http://localhost:8001`
- Notification Service: `http://localhost:8002`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- Kibana: `http://localhost:5601`

Default Grafana credentials:

```text
username: admin
password: admin
```

## Running On EC2 With Docker

The same root `docker-compose.yml` can be used on EC2.

```bash
APP_ENV=PROD \
KAFKA_ADVERTISED_HOST=<ec2-private-ip-or-dns> \
docker compose up -d --build
```

If Kafka is only consumed by containers inside Compose, the internal listener `kafka:29092` is used by the services. `KAFKA_ADVERTISED_HOST` is useful when Kafka also needs to be reachable from outside the Docker network.

## Environment Variables

### Common

| Variable | Default | Purpose |
| --- | --- | --- |
| `APP_ENV` | `DEV` | Controls AWS AppConfig environment for `recurringService` |
| `SPRING_PROFILES_ACTIVE` | `default` | Spring profile |
| `AWS_REGION` | `ap-southeast-2` | AWS SDK region |
| `AWS_DEFAULT_REGION` | `ap-southeast-2` | AWS default region |
| `GRAFANA_ADMIN_USER` | `admin` | Grafana admin user |
| `GRAFANA_ADMIN_PASSWORD` | `admin` | Grafana admin password |

### AWS AppConfig

Used by `recurringService`:

| Variable | Default |
| --- | --- |
| `AWS_APPCONFIG_APPLICATION_IDENTIFIER` | `RecurringNotificationService` |
| `AWS_APPCONFIG_ENVIRONMENT_IDENTIFIER` | `${APP_ENV:-DEV}` |
| `AWS_APPCONFIG_CONFIGURATION_PROFILE_IDENTIFIER` | `NotificationService-Config` |
| `AWS_APPCONFIG_MINIMUM_POLL_INTERVAL_SECONDS` | `30` |

### AWS Secrets Manager

Used by `notificationService`:

| Variable | Default |
| --- | --- |
| `AWS_SECRETSMANAGER_MAIL_SECRET_NAME` | `notification-service/mail` |

On EC2, attach an IAM role or provide valid AWS credentials so the services can access AWS AppConfig and Secrets Manager.

## Observability

### Metrics

Prometheus scrapes:

- `recurring-service:8001/actuator/prometheus`
- `notification-service:8002/actuator/prometheus`

Grafana is provisioned from:

```text
grafana/provisioning
```

### Logs

All services write logs into:

```text
./logs
```

Filebeat reads those logs and sends them to Elasticsearch. Kibana can be used to inspect logs.

## Testing

Run all tests from each service:

```bash
./eurekaServer/mvnw -f eurekaServer/pom.xml test
./gatewayServer/mvnw -f gatewayServer/pom.xml test
./infaService/mvnw -f infaService/pom.xml test
./recurringService/mvnw -f recurringService/pom.xml test
./notificationService/mvnw -f notificationService/pom.xml test
```

Or run tests for a specific service:

```bash
cd recurringService
./mvnw test
```

## CI

GitHub Actions runs tests for all five services on every push using:

```text
.github/workflows/test.yml
```

## Project Structure

```text
.
├── eurekaServer
├── gatewayServer
├── infaService
├── recurringService
├── notificationService
├── local-test
├── grafana
├── docker-compose.yml
├── Dockerfile.service
├── prometheus.yml
├── filebeat.yml
└── pom.xml
```

## Notes

- Use `local-test/docker-compose.yml` when running services from IntelliJ.
- Use root `docker-compose.yml` when running the full stack in Docker.
- Use `APP_ENV=PROD` on EC2 so `recurringService` reads production AppConfig values.
- Ensure AWS permissions are available before running AWS-backed services in production.
