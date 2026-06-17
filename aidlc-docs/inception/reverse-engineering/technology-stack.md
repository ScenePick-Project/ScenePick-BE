# Technology Stack

## Programming Languages
- **Java 21** - Primary application language (LTS version)
- **SQL** - Database queries (Oracle SQL dialect)
- **XML** - MyBatis mapper configuration
- **Groovy** - Gradle build scripts

## Frameworks

### Core Framework
- **Spring Boot 3.5.9** - Application framework
  - Spring Web - REST API development
  - Spring Security - Authentication and authorization
  - Spring Data JPA - ORM support (available but not primary)
  - Spring JDBC - Database connectivity
  - Spring Validation - Request validation

### Data Access
- **MyBatis Spring Boot Starter 3.0.5** - Primary ORM framework
  - SQL mapping
  - Result mapping
  - Dynamic SQL support

### Security
- **Spring Security** - Security framework
- **Spring OAuth2 Client** - OAuth2 integration (Google)
- **JWT (jjwt) 0.12.6** - Token-based authentication
  - jjwt-api - JWT API
  - jjwt-impl - JWT implementation
  - jjwt-jackson - JSON processing for JWT

### Documentation
- **SpringDoc OpenAPI 2.8.8** - API documentation
  - Swagger UI integration
  - OpenAPI 3.0 specification generation

## Infrastructure

### Database
- **Oracle Database** - Primary data store
  - ojdbc11 - Oracle JDBC driver
  - HikariCP - Connection pooling (Spring Boot default)

### Cloud Services
- **AWS S3** - File storage
  - Spring Cloud AWS Starter S3 3.4.0

### External APIs
- **TMDB (The Movie Database)** - Content metadata provider
  - Custom integration via Infrastructure package

## Build Tools
- **Gradle 8.x** - Build automation
  - Gradle Wrapper included
  - Spring Boot Gradle Plugin
  - Spring Dependency Management Plugin

## Development Tools

### Code Generation
- **Lombok** - Boilerplate code reduction
  - @Data, @RequiredArgsConstructor, @Getter, @Setter
  - Compile-time annotation processing

### Object Mapping
- **ModelMapper 3.2.3** - Object-to-object mapping
- **MapStruct 1.6.3** - Compile-time object mapping
  - Annotation processor for type-safe mapping

## Testing Tools
- **JUnit Platform** - Test execution platform
- **Spring Boot Starter Test** - Testing support
  - JUnit 5
  - Mockito
  - AssertJ
  - Spring Test
- **MyBatis Spring Boot Starter Test 3.0.5** - MyBatis testing support

## Runtime Environment
- **Java 21 (LTS)** - Runtime JVM
- **Embedded Tomcat** - Application server (Spring Boot default)
- **Oracle Database** - Production database

## Development Environment
- **IDE Support**: IntelliJ IDEA configurations (.idea folder)
- **Editor Config**: .editorconfig for consistent code formatting
- **Code Style**: Naver checkstyle rules (rule-config/)
  - naver-checkstyle-rules.xml
  - naver-checkstyle-suppressions.xml
  - naver-intellij-formatter.xml

## Version Control
- **Git** - Source control
- **GitHub** - Repository hosting (.github folder present)

## API & Integration Standards
- **REST** - API architectural style
- **JSON** - Data interchange format
- **OAuth2** - Third-party authentication protocol
- **JWT** - Token format for authentication
- **OpenAPI 3.0** - API specification standard

## Security Standards
- **BCrypt** - Password hashing algorithm
- **HTTPS** - Secure communication (production)
- **CORS** - Cross-origin resource sharing
- **HTTP-only Cookies** - Secure token storage

## Database Standards
- **ACID Transactions** - Data consistency
- **Soft Delete Pattern** - Data retention (DEL_YN flag)
- **Timestamp Tracking** - Audit trail (CREATED_AT, UPDATED_AT)
- **Auto-increment IDs** - Primary key generation (Oracle IDENTITY)

## Coding Standards
- **Naver Coding Convention** - Code style guide
- **Checkstyle** - Static code analysis
- **UTF-8 Encoding** - Source file encoding
- **Java 21 Features** - Modern Java syntax (Records, Pattern Matching, etc.)

