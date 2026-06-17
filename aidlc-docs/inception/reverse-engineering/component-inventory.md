# Component Inventory

## Application Packages

### Review Package
- **Purpose**: Review management and social engagement features
- **Components**: Controller, Services (Command/Query), DAOs, VOs, DTOs, MyBatis Mappers
- **Status**: Core review functionality complete, ReviewLike feature needs implementation

### Content Package
- **Purpose**: Content catalog management (movies, TV shows, seasons, episodes, cast)
- **Components**: Controller, Services, DAOs, VOs, DTOs, MyBatis Mappers
- **Status**: Complete

### User Package
- **Purpose**: User account management and authentication
- **Components**: Controller, Services, DAOs, VOs, DTOs, MyBatis Mappers
- **Status**: Complete with OAuth2 and JWT support

### Admin Package
- **Purpose**: Administrative operations for content management
- **Components**: Controller
- **Status**: Basic structure in place

## Infrastructure Packages

### Common Package
- **Purpose**: Shared utilities, configurations, and cross-cutting concerns
- **Sub-packages**:
  - apiPayload: API response standardization and error handling
  - config: Spring configuration classes (Security, Swagger, S3, TMDB, ModelMapper)
  - jwt: JWT token management and authentication filters
  - paging: Cursor-based pagination utilities
  - resolvation: Custom argument resolvers
  - security: OAuth2 and security components
  - swagger: Swagger/OpenAPI customization
  - validation: Custom validators
- **Status**: Complete infrastructure foundation

### Infrastructure Package
- **Purpose**: External service integration
- **Sub-packages**:
  - s3: AWS S3 file storage integration
  - tmdb: TMDB API client for content metadata
- **Status**: Complete

## Shared Packages

### Test Package
- **Purpose**: Test utilities and simple test endpoints
- **Components**: SimpleTestController, SimpleTestService, Test DTOs
- **Status**: Basic test infrastructure

## Database Components

### Tables
- **REVIEW**: Review storage with soft delete support
- **REVIEW_LIKE**: Review like/unlike tracking (many-to-many between USERS and REVIEW)
- **CONTENT**: Content metadata (movies, TV shows)
- **CONTENT_GENRES**: Content genre associations
- **CONTENT_EPISODE**: TV show episodes
- **CONTENT_SEASON**: TV show seasons
- **CONTENT_CREDIT**: Cast and crew information
- **USERS**: User accounts with OAuth2 support
- **USER_REFRESH_TOKEN**: JWT refresh token management
- **simple_test**: Test data table

### MyBatis Mappers
- **ReviewMapper.xml**: Complete SQL queries for review operations
- **ReviewLikeMapper.xml**: Complete SQL queries for review like operations (ready to use)

## Total Count

### Packages by Type
- **Application Packages**: 4 (review, content, user, admin)
- **Infrastructure Packages**: 2 (common, infrastructure)
- **Shared Packages**: 1 (test)
- **Total Packages**: 7

### Components by Layer
- **Controllers**: 4 (Review, Content, User, Admin)
- **Services**: 6+ (ReviewCommand, ReviewQuery, Content services, User services)
- **DAOs**: 6+ (Review, ReviewLike, Content, User, etc.)
- **VOs**: 10+ (Review, ReviewLike, Content, User, etc.)
- **DTOs**: 20+ (Request/Response pairs for each domain)
- **MyBatis Mappers**: 2+ XML files (Review, ReviewLike, others)
- **Configuration Classes**: 7 (Security, Swagger, S3, TMDB, ModelMapper, OpenApiDoc)

### External Dependencies
- **Spring Boot Starters**: 7 (Web, Security, Data JPA, JDBC, Validation, OAuth2 Client)
- **Database**: 1 (Oracle with ojdbc11)
- **ORM**: 2 (MyBatis primary, JPA available)
- **Security**: 2 (Spring Security, JWT)
- **Documentation**: 1 (SpringDoc OpenAPI)
- **Cloud**: 1 (AWS S3)
- **Utilities**: 3 (Lombok, ModelMapper, MapStruct)

## Component Status Summary

### Complete Components
- ✅ Review CRUD operations
- ✅ Content management
- ✅ User authentication (JWT + OAuth2)
- ✅ Security configuration
- ✅ API response standardization
- ✅ Swagger documentation
- ✅ MyBatis SQL mappers (including ReviewLike)
- ✅ Database schema (including REVIEW_LIKE table)

### Incomplete Components (Target for Implementation)
- ❌ ReviewLikeVo.java (empty file)
- ❌ ReviewLikeDao.java (empty file)
- ❌ ReviewLike service layer (not created yet)
- ❌ ReviewLike controller endpoints (not created yet)
- ❌ ReviewLike DTOs (not created yet)

### Implementation Readiness
- **Database**: ✅ REVIEW_LIKE table exists with proper constraints
- **SQL Queries**: ✅ ReviewLikeMapper.xml complete with all necessary queries
- **Infrastructure**: ✅ All supporting infrastructure (security, API response, etc.) ready
- **Pattern Reference**: ✅ Existing Review implementation provides clear pattern to follow

