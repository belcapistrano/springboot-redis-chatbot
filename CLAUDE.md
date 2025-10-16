# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
This is a Spring Boot application with Redis integration. The project is currently empty and ready for development.

## Common Development Commands

### Maven Commands
- `mvn spring-boot:run` - Run the Spring Boot application
- `mvn clean install` - Build the project and run tests
- `mvn test` - Run all tests
- `mvn test -Dtest=ClassName` - Run a specific test class
- `mvn test -Dtest=ClassName#methodName` - Run a specific test method
- `mvn clean` - Clean build artifacts
- `mvn compile` - Compile the source code
- `mvn package` - Package the application as JAR

### Development Workflow
- `mvn spring-boot:run -Dspring-boot.run.profiles=dev` - Run with development profile
- `mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"` - Run with debug mode

## Architecture Guidelines

### Expected Project Structure
```
src/
├── main/
│   ├── java/
│   │   └── com/example/
│   │       ├── Application.java (main class)
│   │       ├── config/
│   │       │   └── RedisConfig.java
│   │       ├── controller/
│   │       ├── service/
│   │       ├── repository/
│   │       └── model/
│   └── resources/
│       ├── application.yml (or .properties)
│       └── application-dev.yml
└── test/
    └── java/
```

### Redis Integration Patterns
- Use `@RedisHash` for entity mapping
- Implement `CrudRepository<Entity, String>` for Redis repositories
- Configure Redis connection in `RedisConfig.java`
- Use `RedisTemplate` for complex operations
- Consider `@Cacheable`, `@CacheEvict` for caching strategies

### Spring Boot Conventions
- Main application class should be annotated with `@SpringBootApplication`
- Controllers use `@RestController` or `@Controller`
- Services use `@Service`
- Configuration classes use `@Configuration`
- Use `@Value` or `@ConfigurationProperties` for external configuration

## Configuration Notes
- Redis connection details typically in `application.yml`
- Use Spring profiles (dev, test, prod) for environment-specific configs
- Default Redis port: 6379
- Default Spring Boot port: 8080