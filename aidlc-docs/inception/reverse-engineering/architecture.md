# System Architecture

## System Overview

ScenePick is a Spring Boot 3.5.9 monolithic application built with Java 21, following a layered architecture pattern. The system uses MyBatis for data access, Spring Security with JWT for authentication, and integrates with external services (TMDB, AWS S3). The architecture follows a standard Controller-Service-DAO pattern with clear separation of concerns.

## Architecture Diagram

```
Text-based Architecture (Mermaid validation deferred):

┌─────────────────────────────────────────────────────────────┐
│                     External Clients                         │
│              (Web Browser, Mobile Apps)                      │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTPS/REST
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   Spring Security Layer                      │
│         (JWT Filter, OAuth2, CORS, Authentication)           │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    Controller Layer                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ Review   │  │ Content  │  │   User   │  │  Admin   │   │
│  │Controller│  │Controller│  │Controller│  │Controller│   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                     Service Layer                            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                  │
│  │ Review   │  │ Content  │  │   User   │                  │
│  │ Services │  │ Services │  │ Services │                  │
│  └──────────┘  └──────────┘  └──────────┘                  │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                      DAO Layer                               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                  │
│  │ Review   │  │ Content  │  │   User   │                  │
│  │   DAO    │  │   DAO    │  │   DAO    │                  │
│  └──────────┘  └──────────┘  └──────────┘                  │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   MyBatis Mapper Layer                       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                  │
│  │ Review   │  │ Content  │  │   User   │                  │
│  │  Mapper  │  │  Mapper  │  │  Mapper  │                  │
│  └──────────┘  └──────────┘  └──────────┘                  │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    Oracle Database                           │
│  (REVIEW, REVIEW_LIKE, CONTENT, USERS, etc.)                │
└─────────────────────────────────────────────────────────────┘

External Integrations:
┌──────────┐         ┌──────────┐
│   TMDB   │◄────────┤Infrastructure│
│   API    │         │   Package    │
└──────────┘         └──────────────┘
                            │
                            ▼
                     ┌──────────┐
                     │  AWS S3  │
                     └──────────┘
```

## Component Descriptions

### Review Package
- **Purpose**: Manages review lifecycle and social engagement
- **Responsibilities**: 
  - Review CRUD operations
  - Cursor-based pagination
  - Review like/unlike (target feature)
  - Multimedia attachment handling
- **Dependencies**: User (authentication), Content (review target)
- **Type**: Application

### Content Package
- **Purpose**: Content catalog management
- **Responsibilities**:
  - Content metadata retrieval
  - Season/episode management
  - Cast information
  - Genre management
- **Dependencies**: Infrastructure (TMDB integration)
- **Type**: Application

### User Package
- **Purpose**: User account and authentication management
- **Responsibilities**:
  - User registration/login
  - JWT token lifecycle
  - OAuth2 integration
  - Refresh token management
- **Dependencies**: Common (JWT, Security)
- **Type**: Application

### Common Package
- **Purpose**: Shared infrastructure and utilities
- **Responsibilities**:
  - API response standardization (ApiResponse)
  - JWT authentication (JwtTokenProvider, JwtAuthenticationFilter)
  - Security configuration (SecurityConfig)
  - Error handling (ExceptionAdvice, GeneralException)
  - Swagger/OpenAPI configuration
  - Validation utilities
- **Dependencies**: None (foundation layer)
- **Type**: Infrastructure/Utilities

### Infrastructure Package
- **Purpose**: External service integration
- **Responsibilities**:
  - AWS S3 file operations
  - TMDB API client
  - External service configurations
- **Dependencies**: Common (configuration)
- **Type**: Infrastructure

### Admin Package
- **Purpose**: Administrative operations
- **Responsibilities**:
  - Content administration
  - System management
- **Dependencies**: Content, User
- **Type**: Application

## Data Flow

### Review Like Flow (Target Feature)
```
User Request → Security Filter → ReviewController
    → ReviewCommandService → ReviewLikeDao
    → MyBatis Mapper → Oracle DB
    → Response back through layers
```

### Authentication Flow
```
Login Request → SecurityConfig → OAuth2/JWT
    → JwtTokenProvider → UserService
    → Database → JWT Token Response
```

### Review Creation Flow
```
Authenticated User → ReviewController
    → ReviewCommandService → ReviewDao
    → MyBatis Mapper → Oracle DB
    → Response with reviewId
```

## Integration Points

### External APIs
- **TMDB API**: Content metadata retrieval (movies, TV shows, cast, images)
- **Google OAuth2**: Third-party authentication provider
- **AWS S3**: File storage for user-uploaded content

### Databases
- **Oracle Database**: Primary data store for all entities
  - Tables: REVIEW, REVIEW_LIKE, CONTENT, USERS, USER_REFRESH_TOKEN, etc.
  - MyBatis for SQL mapping
  - Spring Data JPA available but MyBatis is primary

### Third-party Services
- **Spring Security**: Authentication and authorization framework
- **JWT (jjwt)**: Token-based authentication
- **SpringDoc OpenAPI**: API documentation (Swagger UI)
- **Lombok**: Code generation for boilerplate
- **ModelMapper/MapStruct**: Object mapping utilities

## Infrastructure Components

### Deployment Model
- **Type**: Monolithic Spring Boot application
- **Packaging**: JAR (Spring Boot embedded Tomcat)
- **Build Tool**: Gradle 8.x
- **Java Version**: 21 (LTS)

### Security Architecture
- **Authentication**: JWT + OAuth2
- **Session Management**: Stateless (JWT-based)
- **CORS**: Configured for localhost:5173 (development)
- **Password Encoding**: BCrypt
- **Token Storage**: HTTP-only cookies

### Database Architecture
- **Database**: Oracle (ojdbc11)
- **ORM**: MyBatis 3.0.5 (primary), Spring Data JPA (available)
- **Connection Pooling**: HikariCP (Spring Boot default)
- **Transaction Management**: Spring @Transactional

