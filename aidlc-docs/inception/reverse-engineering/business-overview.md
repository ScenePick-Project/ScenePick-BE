# Business Overview

## Business Context

ScenePick is a content review and recommendation platform that allows users to discover, review, and share their opinions about movies and TV shows. The system integrates with TMDB (The Movie Database) for content metadata and provides social features for user engagement.

## Business Description

ScenePick enables users to:
- Browse and search for movies and TV shows with detailed information
- Write and share reviews with multimedia attachments (YouTube videos, music tracks)
- Interact with other users' reviews through likes and recommendations
- Manage their profiles and authentication through multiple providers (OAuth2, traditional login)
- Access content information including cast, seasons, and episodes

## Business Transactions

### 1. User Management
- **User Registration**: New users sign up with email/password or OAuth2 providers (Google)
- **User Authentication**: Users log in and receive JWT tokens for session management
- **Profile Management**: Users manage their account information and preferences

### 2. Content Discovery
- **Content Browsing**: Users browse movies and TV shows with metadata from TMDB
- **Content Details**: Users view detailed information including synopsis, cast, seasons, and episodes
- **Genre Filtering**: Users filter content by genres

### 3. Review Management
- **Review Creation**: Users write reviews for content with optional multimedia attachments
- **Review Reading**: Users browse and read reviews from other users
- **Review Deletion**: Users can delete their own reviews
- **Review Pagination**: System provides cursor-based pagination for efficient review browsing

### 4. Social Engagement (ReviewLike - Target Feature)
- **Review Liking**: Users can like/unlike reviews to show appreciation
- **Like Count**: System tracks and displays the number of likes per review
- **Like Status**: Users can check if they've already liked a specific review

### 5. Content Administration
- **Content Management**: Administrators manage content catalog and metadata

## Business Dictionary

### Core Entities
- **Content**: A movie or TV show with metadata (title, poster, synopsis, genres)
- **Review**: User-generated content critique with optional multimedia (YouTube video, music track)
- **User**: Registered platform member with authentication credentials
- **ReviewLike**: Social engagement indicator showing user appreciation for reviews
- **Episode**: Individual episode within a TV show season
- **Season**: Collection of episodes within a TV show
- **Credit**: Cast and crew information for content

### Business Rules
- **Soft Delete**: Reviews are marked as deleted (DEL_YN='Y') rather than physically removed
- **Spoiler Protection**: Reviews can be marked as containing spoilers
- **Authentication Required**: Most operations require authenticated users
- **Ownership Validation**: Users can only delete their own reviews
- **Unique Constraints**: One like per user per review (prevents duplicate likes)

### Technical Terms
- **TMDB**: The Movie Database - external content metadata provider
- **JWT**: JSON Web Token - authentication mechanism
- **OAuth2**: Third-party authentication protocol (Google)
- **Cursor Pagination**: Efficient pagination using timestamp and ID cursors
- **Soft Delete**: Logical deletion using flag instead of physical removal

## Component Level Business Descriptions

### Review Package
- **Purpose**: Manages user-generated reviews and social engagement features
- **Responsibilities**: 
  - Review CRUD operations
  - Review pagination and filtering
  - Review like/unlike functionality (target feature)
  - Multimedia attachment handling (YouTube, music tracks)

### Content Package
- **Purpose**: Manages content catalog and metadata
- **Responsibilities**:
  - Content information retrieval
  - Season and episode management
  - Cast and crew information
  - Genre categorization

### User Package
- **Purpose**: Manages user accounts and authentication
- **Responsibilities**:
  - User registration and login
  - JWT token management
  - OAuth2 integration
  - Profile management

### Common Package
- **Purpose**: Provides shared utilities and infrastructure
- **Responsibilities**:
  - API response standardization
  - JWT authentication filters
  - Security configuration
  - Error handling
  - Swagger documentation

### Infrastructure Package
- **Purpose**: Integrates with external services
- **Responsibilities**:
  - AWS S3 file storage
  - TMDB API integration
  - External service configurations

### Admin Package
- **Purpose**: Administrative operations
- **Responsibilities**:
  - Content administration
  - System management

