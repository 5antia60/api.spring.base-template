## Base Template API

Base template for creating CRUD APIs with **Spring Boot**, already equipped with authentication, security, pagination, migrations, and documentation.

## Features

- JWT Authentication
- Access control with Spring Security
- User CRUD
- Task CRUD
- Standardized pagination
- Global exception handling
- Migrations with Flyway
- Documentation with Swagger / OpenAPI
- PostgreSQL integration

## How to run the project

### Prerequisites

- Java 21+
- PostgreSQL
- Maven or Maven Wrapper

### 1. Clone the project
```
$ git clone <REPOSITORY_URL>
$ cd api.spring.base-template
```

### 2. Create the database

Create a database in PostgreSQL for the application.

Example:
```sql
CREATE DATABASE database_name;
```

### 3. Configure `application.properties`

Create the file:
```
src/main/resources/application.properties
```
Use the `application.properties.example` file as a template.


### 4. Run the application

On Windows:
```
$ mvnw.cmd spring-boot:run
```

Or with Maven installed:
```
$ mvn spring-boot:run
``` 

### 5. Access the application

- API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui/index.html`

## Migrations

Migrations are located in:
```
src/main/resources/db/migration
```

To create a new migration, add a new file following the pattern:
```
V3__migration_name.sql
``` 

Flyway runs automatically when the application starts up, provided that `spring.flyway.enabled=true`.
