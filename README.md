# FMP Job Service

This project is a Spring Boot-based microservice for managing job types, job tags, and job tasks in the FSM (Field Service Management) system. It provides RESTful APIs for CRUD operations and integrates with other FSM modules.

## Project Structure

- **src/main/java/com/octal/fsm/**
  - **controller/**: REST controllers for job types, jobs, and job tags.
  - **dto/**: Data Transfer Objects for API requests/responses.
  - **entities/**: JPA entities for persistence.
  - **exceptions/**: Custom exception classes and handlers.
  - **repositories/**: Spring Data JPA repositories.
  - **service/**: Service interfaces and implementations.
  - **specification/**: Classes for dynamic query specifications.
  - **transformer/**: DTO/entity transformers.
  - **utils/**: Utility classes.
- **src/main/resources/**
  - **application.yaml**: Main configuration file.
  - **application-*.yml**: Environment-specific configs.

## Main Features
- Manage Job Types, Job Tags, and Job Tasks
- CRUD APIs for jobs and related entities
- Exception handling and validation
- OpenAPI/Swagger configuration

## Getting Started

### Prerequisites
- Java 11+
- Maven
- Database (configured in `application.yaml`)

### Build & Run
```bash
mvn clean install
mvn spring-boot:run
```

### API Documentation
Swagger UI is available at `/swagger-ui.html` when the service is running.

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
This project is licensed under the MIT License.

