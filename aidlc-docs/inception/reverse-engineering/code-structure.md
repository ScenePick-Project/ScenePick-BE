# Code Structure

## Build System
- **Type**: Gradle 8.x
- **Configuration**: build.gradle, settings.gradle
- **Key Dependencies**:
  - Spring Boot 3.5.9 (Web, Security, Data JPA, JDBC, Validation)
  - MyBatis Spring Boot Starter 3.0.5
  - Oracle JDBC Driver (ojdbc11)
  - JWT (jjwt 0.12.6)
  - Lombok, ModelMapper, MapStruct
  - SpringDoc OpenAPI 2.8.8
  - AWS Spring Cloud S3 3.4.0

## Package Structure

```
com.project.scenepickbe/
├── ScenePickBeApplication.java          # Spring Boot main class
│
├── admin/                               # Administrative functionality
│   └── controller/
│       └── ContentAdminController.java
│
├── common/                              # Shared utilities and infrastructure
│   ├── BaseVo.java                      # Base value object
│   ├── apiPayload/                      # API response standardization
│   │   ├── ApiResponse.java             # Standard API response wrapper
│   │   └── code/                        # Response codes and error handling
│   │       ├── BaseCode.java
│   │       ├── BaseErrorCode.java
│   │       ├── ErrorReasonDTO.java
│   │       ├── ReasonDTO.java
│   │       ├── exception/
│   │       │   ├── ExceptionAdvice.java # Global exception handler
│   │       │   └── GeneralException.java
│   │       └── status/
│   │           ├── ErrorStatus.java
│   │           └── SuccessStatus.java
│   ├── config/                          # Configuration classes
│   │   ├── ModelMapperConfig.java
│   │   ├── OpenApiDocCustomizerConfig.java
│   │   ├── S3Config.java
│   │   ├── SecurityConfig.java          # Spring Security configuration
│   │   ├── SwaggerConfig.java
│   │   └── TmdbConfig.java
│   ├── jwt/                             # JWT authentication
│   │   ├── CookieProvider.java
│   │   ├── JwtAuthenticationEntryPoint.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtTokenProvider.java
│   │   └── dto/
│   │       ├── JwtToken.java
│   │       └── RefreshPayload.java
│   ├── paging/                          # Pagination utilities
│   │   └── CursorPaging.java
│   ├── resolvation/                     # Custom resolvers
│   │   ├── annotation/
│   │   └── resolver/
│   ├── security/                        # Security components
│   │   └── oauth/
│   │       ├── CustomerOauth2UserService.java
│   │       ├── GoogleUserDetails.java
│   │       └── OAuth2SuccessHandler.java
│   ├── swagger/                         # Swagger customization
│   │   └── DocSuccess.java
│   └── validation/                      # Custom validators
│
├── content/                             # Content management
│   ├── controller/
│   │   └── ContentController.java       # Content API endpoints
│   ├── dao/                             # Data access objects
│   ├── dto/                             # Data transfer objects
│   │   ├── request/
│   │   └── response/
│   ├── enums/                           # Content-related enums
│   ├── service/                         # Business logic
│   └── vo/                              # Value objects
│
├── infrastructure/                      # External service integration
│   ├── s3/                              # AWS S3 integration
│   └── tmdb/                            # TMDB API integration
│
├── review/                              # Review management (TARGET PACKAGE)
│   ├── controller/
│   │   └── ReviewController.java        # Review API endpoints
│   ├── dao/
│   │   ├── ReviewDao.java               # Review data access
│   │   └── ReviewLikeDao.java           # ReviewLike data access (EMPTY - TARGET)
│   ├── dto/
│   │   ├── request/
│   │   │   └── ReviewRequest.java       # Review request DTOs
│   │   └── response/
│   │       └── ReviewResponse.java      # Review response DTOs
│   ├── mapper/                          # MyBatis mappers (not used, XML preferred)
│   ├── service/
│   │   ├── ReviewCommandService.java    # Review write operations
│   │   └── ReviewQueryService.java      # Review read operations
│   └── vo/
│       ├── ReviewVo.java                # Review value object
│       └── ReviewLikeVo.java            # ReviewLike value object (EMPTY - TARGET)
│
├── test/                                # Test utilities
│   ├── SimpleTestController.java
│   ├── SimpleTestListResDto.java
│   ├── SimpleTestService.java
│   └── vo/
│
└── user/                                # User management
    ├── controller/
    │   └── UserController.java          # User API endpoints
    ├── dao/                             # User data access
    ├── dto/                             # User DTOs
    │   ├── request/
    │   └── response/
    ├── enums/                           # User-related enums
    ├── service/                         # User business logic
    └── vo/                              # User value objects
```

## MyBatis Mapper Files

```
src/main/resources/mybatis/mapper/
├── ReviewMapper.xml                     # Review SQL queries
└── ReviewLikeMapper.xml                 # ReviewLike SQL queries (COMPLETE - TARGET)
```

## Database Schema

```
src/main/resources/sql/
└── schema.sql                           # Complete database schema including REVIEW_LIKE table
```

## Existing Files Inventory (Review Package - Target Area)

### Controllers
- `review/controller/ReviewController.java` - REST API endpoints for review operations
  - GET /api/v1/reviews/{reviewId} - Get single review
  - GET /api/v1/contents/{contentId}/reviews - Get review list with cursor pagination
  - POST /api/v1/contents/{contentId}/reviews - Create review
  - DELETE /api/v1/reviews/{reviewId} - Delete review

### Services
- `review/service/ReviewCommandService.java` - Review write operations (create, delete)
- `review/service/ReviewQueryService.java` - Review read operations (get, list)

### DAOs
- `review/dao/ReviewDao.java` - Review data access interface
- `review/dao/ReviewLikeDao.java` - **EMPTY FILE - TARGET FOR IMPLEMENTATION**

### Value Objects
- `review/vo/ReviewVo.java` - Review entity mapping
- `review/vo/ReviewLikeVo.java` - **EMPTY FILE - TARGET FOR IMPLEMENTATION**

### DTOs
- `review/dto/request/ReviewRequest.java` - Review request DTOs (Create, Slice)
- `review/dto/response/ReviewResponse.java` - Review response DTOs (Created, Review, ReviewList, Cursor, SliceList)

### MyBatis Mappers
- `mybatis/mapper/ReviewMapper.xml` - Review SQL queries (complete)
- `mybatis/mapper/ReviewLikeMapper.xml` - **COMPLETE SQL QUERIES - READY TO USE**
  - insertReviewLike - Add like
  - deleteReviewLike - Remove like
  - existsReviewLike - Check if user liked review
  - countReviewLikes - Get like count for review
  - selectReviewLike - Get like information

## Design Patterns

### Layered Architecture Pattern
- **Location**: Entire application
- **Purpose**: Separation of concerns, maintainability
- **Implementation**: Controller → Service → DAO → MyBatis Mapper → Database

### DTO Pattern
- **Location**: All packages (dto/request, dto/response)
- **Purpose**: Data transfer between layers, API contract definition
- **Implementation**: Java Records for immutability

### Value Object Pattern
- **Location**: All packages (vo/)
- **Purpose**: Database entity representation
- **Implementation**: POJOs with Lombok annotations, MyBatis @Alias

### Repository Pattern (DAO)
- **Location**: All packages (dao/)
- **Purpose**: Data access abstraction
- **Implementation**: MyBatis interfaces with XML mappers

### Command-Query Separation (CQS)
- **Location**: Service layer (ReviewCommandService, ReviewQueryService)
- **Purpose**: Separate read and write operations
- **Implementation**: Separate service classes for commands and queries

### Dependency Injection
- **Location**: All components
- **Purpose**: Loose coupling, testability
- **Implementation**: Spring @RequiredArgsConstructor with Lombok

### Global Exception Handling
- **Location**: common/apiPayload/code/exception/ExceptionAdvice.java
- **Purpose**: Centralized error handling
- **Implementation**: Spring @RestControllerAdvice

### API Response Wrapper
- **Location**: common/apiPayload/ApiResponse.java
- **Purpose**: Consistent API response format
- **Implementation**: Generic wrapper with success/error handling

### Filter Chain Pattern
- **Location**: common/jwt/JwtAuthenticationFilter.java
- **Purpose**: Request preprocessing (authentication)
- **Implementation**: Spring Security filter chain

### Strategy Pattern
- **Location**: common/security/oauth/
- **Purpose**: Multiple authentication strategies (OAuth2, JWT)
- **Implementation**: Spring Security with custom handlers

## Critical Dependencies

### Spring Boot 3.5.9
- **Version**: 3.5.9
- **Usage**: Core framework for entire application
- **Purpose**: Dependency injection, web framework, security, data access

### MyBatis Spring Boot Starter 3.0.5
- **Version**: 3.0.5
- **Usage**: Primary data access layer
- **Purpose**: SQL mapping, database operations
- **Note**: Preferred over JPA in this project

### Oracle JDBC Driver (ojdbc11)
- **Version**: Latest (runtime dependency)
- **Usage**: Database connectivity
- **Purpose**: Oracle database access

### JWT (jjwt) 0.12.6
- **Version**: 0.12.6
- **Usage**: Authentication tokens
- **Purpose**: Stateless authentication, token generation/validation

### Lombok
- **Version**: Managed by Spring Boot
- **Usage**: All entity and DTO classes
- **Purpose**: Reduce boilerplate code (@Data, @RequiredArgsConstructor, etc.)

### SpringDoc OpenAPI 2.8.8
- **Version**: 2.8.8
- **Usage**: API documentation
- **Purpose**: Swagger UI, OpenAPI specification generation

### AWS Spring Cloud S3 3.4.0
- **Version**: 3.4.0
- **Usage**: File storage
- **Purpose**: AWS S3 integration for file uploads

### ModelMapper 3.2.3 & MapStruct 1.6.3
- **Version**: ModelMapper 3.2.3, MapStruct 1.6.3
- **Usage**: Object mapping
- **Purpose**: VO to DTO conversion

## Code Quality Indicators

### Naming Conventions
- **Controllers**: *Controller suffix
- **Services**: *Service suffix (Command/Query separation)
- **DAOs**: *Dao suffix
- **VOs**: *Vo suffix
- **DTOs**: Nested records in *Request/*Response classes
- **Mappers**: *Mapper.xml files

### Package Organization
- Clear separation by feature (review, content, user)
- Common utilities in shared package
- Infrastructure concerns isolated

### API Design
- RESTful endpoints
- Consistent response format (ApiResponse wrapper)
- Swagger documentation on all endpoints
- Validation annotations on request DTOs

