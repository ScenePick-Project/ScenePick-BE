# Dependencies

## Internal Dependencies

```
Text-based Dependency Diagram (Mermaid validation deferred):

┌─────────────────────────────────────────────────────────────┐
│                      Application Layer                       │
│                                                               │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐              │
│  │  Review  │    │ Content  │    │   User   │              │
│  │ Package  │    │ Package  │    │ Package  │              │
│  └────┬─────┘    └────┬─────┘    └────┬─────┘              │
│       │               │               │                      │
│       └───────────────┼───────────────┘                      │
│                       │                                      │
└───────────────────────┼──────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│                   Infrastructure Layer                       │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Common Package                          │   │
│  │  • API Response (ApiResponse)                        │   │
│  │  • Security (JWT, OAuth2)                            │   │
│  │  • Configuration (Security, Swagger, S3, TMDB)       │   │
│  │  • Error Handling (ExceptionAdvice)                  │   │
│  │  • Validation                                        │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         Infrastructure Package                       │   │
│  │  • S3 Integration                                    │   │
│  │  • TMDB Integration                                  │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│                    External Services                         │
│  • Oracle Database                                           │
│  • AWS S3                                                    │
│  • TMDB API                                                  │
│  • Google OAuth2                                             │
└─────────────────────────────────────────────────────────────┘
```

## Package Dependencies

### Review Package Dependencies
- **Depends on**:
  - Common Package (ApiResponse, JWT authentication, error handling)
  - User Package (user authentication, userId validation)
  - Content Package (contentId validation for reviews)
- **Type**: Compile-time
- **Reason**: 
  - Needs standard API response format
  - Requires user authentication for review operations
  - Validates content existence before creating reviews

### Content Package Dependencies
- **Depends on**:
  - Common Package (ApiResponse, configuration)
  - Infrastructure Package (TMDB integration for metadata)
- **Type**: Compile-time
- **Reason**:
  - Needs standard API response format
  - Fetches content metadata from TMDB API

### User Package Dependencies
- **Depends on**:
  - Common Package (ApiResponse, JWT, OAuth2, security configuration)
- **Type**: Compile-time
- **Reason**:
  - Needs JWT token generation and validation
  - Requires OAuth2 integration for Google login
  - Uses security configuration for authentication

### Admin Package Dependencies
- **Depends on**:
  - Common Package (ApiResponse, security)
  - Content Package (content management operations)
  - User Package (admin role validation)
- **Type**: Compile-time
- **Reason**:
  - Needs admin authentication and authorization
  - Manages content through Content package services

### Infrastructure Package Dependencies
- **Depends on**:
  - Common Package (configuration classes)
- **Type**: Compile-time
- **Reason**:
  - Uses configuration for S3 and TMDB setup

### Common Package Dependencies
- **Depends on**:
  - Spring Boot Framework
  - Spring Security
  - JWT libraries
- **Type**: Compile-time
- **Reason**:
  - Foundation layer providing infrastructure for all other packages

## External Dependencies

### Spring Boot 3.5.9
- **Version**: 3.5.9
- **Purpose**: Core application framework
- **License**: Apache License 2.0
- **Modules Used**:
  - spring-boot-starter-web
  - spring-boot-starter-security
  - spring-boot-starter-data-jpa
  - spring-boot-starter-jdbc
  - spring-boot-starter-validation
  - spring-boot-starter-oauth2-client

### MyBatis Spring Boot Starter 3.0.5
- **Version**: 3.0.5
- **Purpose**: SQL mapping and data access
- **License**: Apache License 2.0
- **Usage**: Primary ORM for all database operations

### Oracle JDBC Driver (ojdbc11)
- **Version**: Latest (runtime)
- **Purpose**: Oracle database connectivity
- **License**: Oracle Free Use Terms and Conditions
- **Usage**: Database driver for all data persistence

### JWT (jjwt) 0.12.6
- **Version**: 0.12.6
- **Purpose**: JWT token generation and validation
- **License**: Apache License 2.0
- **Modules**:
  - jjwt-api (compile)
  - jjwt-impl (runtime)
  - jjwt-jackson (runtime)

### Lombok
- **Version**: Managed by Spring Boot
- **Purpose**: Reduce boilerplate code
- **License**: MIT License
- **Usage**: All entity, VO, and DTO classes

### SpringDoc OpenAPI 2.8.8
- **Version**: 2.8.8
- **Purpose**: API documentation generation
- **License**: Apache License 2.0
- **Usage**: Swagger UI and OpenAPI specification

### AWS Spring Cloud S3 3.4.0
- **Version**: 3.4.0
- **Purpose**: AWS S3 integration
- **License**: Apache License 2.0
- **Usage**: File storage operations

### ModelMapper 3.2.3
- **Version**: 3.2.3
- **Purpose**: Object-to-object mapping
- **License**: Apache License 2.0
- **Usage**: VO to DTO conversions

### MapStruct 1.6.3
- **Version**: 1.6.3
- **Purpose**: Compile-time object mapping
- **License**: Apache License 2.0
- **Usage**: Type-safe object mapping with annotation processing

### JUnit Platform
- **Version**: Managed by Spring Boot
- **Purpose**: Test execution
- **License**: Eclipse Public License 2.0
- **Usage**: Unit and integration testing

## Dependency Graph by Layer

### Layer 1: External Services (No dependencies on application)
- Oracle Database
- AWS S3
- TMDB API
- Google OAuth2

### Layer 2: Infrastructure (Depends on Layer 1)
- Common Package
  - Spring Boot Framework
  - Spring Security
  - JWT libraries
  - MyBatis
- Infrastructure Package
  - AWS SDK (S3)
  - HTTP Client (TMDB)

### Layer 3: Application (Depends on Layer 2)
- User Package
  - Common Package (JWT, OAuth2, ApiResponse)
- Content Package
  - Common Package (ApiResponse, Configuration)
  - Infrastructure Package (TMDB)
- Review Package
  - Common Package (ApiResponse, JWT)
  - User Package (authentication)
  - Content Package (content validation)
- Admin Package
  - Common Package (security)
  - Content Package (content management)
  - User Package (authorization)

## Circular Dependency Prevention

### Design Principles
- **Layered Architecture**: Clear separation between infrastructure and application layers
- **Dependency Inversion**: Application depends on abstractions (interfaces) not implementations
- **One-way Dependencies**: Dependencies flow from application → infrastructure → external services

### No Circular Dependencies Detected
- Review → User (one-way)
- Review → Content (one-way)
- Content → Infrastructure (one-way)
- All packages → Common (one-way)

## Build Dependencies

### Compile Dependencies
- Spring Boot starters
- MyBatis
- JWT libraries
- Lombok (annotation processor)
- MapStruct (annotation processor)
- ModelMapper
- SpringDoc OpenAPI
- AWS S3 SDK

### Runtime Dependencies
- Oracle JDBC driver
- JWT implementation libraries

### Test Dependencies
- Spring Boot Test
- MyBatis Test
- JUnit Platform

### Annotation Processors
- Lombok
- MapStruct

## Dependency Management Strategy

### Version Management
- **Spring Boot BOM**: Manages most dependency versions
- **Explicit Versions**: Only for non-Spring dependencies (MyBatis, JWT, SpringDoc, etc.)
- **Gradle Dependency Management Plugin**: Ensures consistent versions

### Dependency Scope
- **Compile**: Most application dependencies
- **Runtime**: Database drivers, JWT implementation
- **Test**: Testing frameworks
- **Annotation Processor**: Lombok, MapStruct

### Transitive Dependencies
- Managed by Spring Boot dependency management
- No version conflicts detected
- All transitive dependencies compatible with Java 21

