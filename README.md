# tracking-system
This is a task management app built for the HMCTS DTS Developer Challenge. It helps caseworkers stay on top of their daily tasks with a simple, easy-to-use interface and a reliable backend that keeps everything running smoothly.

Backend Documentation (Spring Boot with H2)
Repository: https://github.com/uumeevuruo/tracking-system-backend

Features
REST API for case management (CRUD operations)

H2 in-memory database (auto-creates tables via JPA)

Data validation and error handling

Unit/Integration tests (85% coverage)

##Setup
Requirements: Java 17+, Maven

Run: mvn spring-boot:run

API: http://localhost:8081/api/v1/case

H2 Console: http://localhost:8081/h2-console
(JDBC URL: jdbc:h2:mem:caseworkerTestDB, credentials in application.properties)

##API Endpoints
swagger: http://localhost:8081/swagger-ui/index.html
Method	Endpoint	Description
POST	/api/v1/case Create new case
GET	/api/v1/case	List all cases
GET	/api/v1/case/{id}	Get case by ID
PUT	/api/v1/case/{id}	Update case
DELETE	/api/v1/case/{id}	Delete case

##Testing
Run: mvn test

##Setup Options
You can use your favourite database by editing the properties below in application.properties
# application.properties using Postgres DB
spring.datasource.url=jdbc:postgresql://localhost:5432/caseworkerTestDB
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=postgres
spring.datasource.password=yourpassword
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDial

Or by adding these properties using environment variable
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/caseworkerTestDB
export SPRING_DATASOURCE_DRIVER=org.postgresql.Driver
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=yourpassword
export SPRING_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDial

To switch from H2 to PostgreSQL: Add PostgreSQL dependency below to pom.xml:
<dependency>
<groupId>org.postgresql</groupId>
<artifactId>postgresql</artifactId>
</dependency>

##H2 Console Access
URL: http://localhost:8080/h2-console

Use JDBC URL: jdbc:h2:mem:caseworkerTestDB
(Credentials match your .env/application.properties)


##Advanced Database Options
Uncomment these for development/debugging:
# Initialize database with data.sql
spring.jpa.defer-datasource-initialization=true
# SQL Logging Configuration
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE