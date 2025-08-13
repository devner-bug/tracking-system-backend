 # Tracking System

A task management app built for the **HMCTS DTS Developer Challenge**. Helps caseworkers manage daily tasks with a simple interface and reliable backend.

---

## Table of Contents

- [Backend Features](#backend-features)
- [Setup](#setup)
- [API Documentation](#api-documentation)
- [Database Configuration](#database-configuration)
- [Testing](#testing)
- [Advanced Options](#advanced-options)

---

## Backend Features

- REST API for case management (CRUD operations)
- H2 in-memory database (auto-creates tables via JPA)
- Data validation and error handling
- Unit/Integration tests (85% coverage)

---

## Setup

**Requirements:**

- Java 17+
- Maven

**Run the application:**

```bash
mvn spring-boot:run
```

**Access Points:**

- API:

[`http://localhost:8081/api/v1/case`](http://localhost:8081/api/v1/case)
- H2 Console:

[`http://localhost:8081/h2-console`](http://localhost:8081/h2-console)  
JDBC URL: `jdbc:h2:mem:caseworkerTestDB`  
*(Credentials in `application.properties`)*

---

## API Documentation

Swagger UI:

[`http://localhost:8081/swagger-ui/index.html`](http://localhost:8081/swagger-ui/index.html)

| Method \| Endpoint \| Description \|
|--------\|-----------------------\|-------------------\|
| POST   \| `/api/v1/case`        \| Create new case   \|
| GET    \| `/api/v1/case`        \| List all cases    \|
| GET    \| `/api/v1/case/{id}`   \| Get case by ID    \|
| PUT    \| `/api/v1/case/{id}`   \| Update case       \|
| DELETE \| `/api/v1/case/{id}`   \| Delete case       \|

---

## Testing

Run all tests:

```bash
mvn test
```

---

## Advanced Options

###  Switch to PostgreSQL

Update `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/caseworkerTestDB
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=postgres
spring.datasource.password=yourpassword
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

Or use environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/caseworkerTestDB
export SPRING_DATASOURCE_DRIVER=org.postgresql.Driver
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=yourpassword
export SPRING_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
```

Add PostgreSQL dependency to `pom.xml`:

```xml
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
</dependency>
```

---

## ️ Database Configuration

- H2 Console:

[`http://localhost:8081/h2-console`](http://localhost:8081/h2-console)
- JDBC URL: `jdbc:h2:mem:caseworkerTestDB`  
  *(Credentials match `.env` or `application.properties`)*

---

## Advanced Database Options

Uncomment the following in `application.properties` for development/debugging:

```properties

# SQL Logging Configuration
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

---
