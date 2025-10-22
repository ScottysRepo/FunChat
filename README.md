# FunChat - Client Server Architecture Style
This is the backend of FunChat implementing by using Client Server architecture style.  
This file provides instructions to run and test.

Prerequisites:

- Java 17+ (or the version your Spring Boot project uses)
- Maven (or use the included Maven Wrapper ./mvnw)
- PostgreSQL (database)
- SwaggerUI

---

## 1. Clone the repository

```bash
git clone https://github.com/YourUsername/FunChat.git
cd FunChat
git checkout client-server-backend
```

## 2. Create Database
Create database in PostgreSQL named fun_chat.
Update the `application.yml` file with your PostgreSQL database credentials.
```properties
spring:
    datasource:
        url: jdbc:postgresql://localhost:5432/hask_task
        username: postgres
        password: postgres1
```

## 3. Swagger UI
Use http://localhost:8080/swagger-ui/index.html to check API.