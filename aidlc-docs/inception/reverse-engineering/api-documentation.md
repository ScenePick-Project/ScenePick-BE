# API Documentation

## REST APIs

### Review APIs

#### Get Single Review
- **Method**: GET
- **Path**: /api/v1/reviews/{reviewId}
- **Purpose**: Retrieve a single review by ID
- **Authentication**: Required
- **Request**: Path parameter reviewId (Long)
- **Response**: ApiResponse<ReviewResponse.Review>
  ```json
  {
    "isSuccess": true,
    "code": "200",
    "message": "OK",
    "result": {
      "reviewId": 1,
      "contentId": 100,
      "userId": "user123",
      "reviewBody": "Great movie!",
      "startTime": 120,
      "endTime": 180,
      "youtubeId": "abc123",
      "isSpoiler": false,
      "trackId": "track456"
    }
  }
  ```

#### Get Review List (Cursor Pagination)
- **Method**: GET
- **Path**: /api/v1/contents/{contentId}/reviews
- **Purpose**: Retrieve paginated list of reviews for a content
- **Authentication**: Required
- **Request**: 
  - Path parameter: contentId (Long)
  - Query parameters:
    - size (Integer, default: 10) - Number of reviews per page
    - cursorCreatedAt (LocalDateTime) - Last review creation time from previous page
    - cursorReviewId (Long) - Last review ID from previous page
- **Response**: ApiResponse<ReviewResponse.SliceList>
  ```json
  {
    "isSuccess": true,
    "code": "200",
    "message": "OK",
    "result": {
      "reviewList": [...],
      "nextCursor": {
        "createdAt": "2026-02-27T10:30:00",
        "reviewId": 50
      },
      "hasNext": true
    }
  }
  ```

#### Create Review
- **Method**: POST
- **Path**: /api/v1/contents/{contentId}/reviews
- **Purpose**: Create a new review for a content
- **Authentication**: Required (user from JWT token)
- **Request**: 
  - Path parameter: contentId (Long)
  - Body: ReviewRequest.Create
  ```json
  {
    "reviewBody": "Amazing cinematography!",
    "isSpoiler": false,
    "trackId": "spotify:track:123",
    "youtubeId": "dQw4w9WgXcQ",
    "startTime": 60,
    "endTime": 120
  }
  ```
- **Response**: ApiResponse<ReviewResponse.Created>
  ```json
  {
    "isSuccess": true,
    "code": "200",
    "message": "OK",
    "result": {
      "reviewId": 123
    }
  }
  ```

#### Delete Review
- **Method**: DELETE
- **Path**: /api/v1/reviews/{reviewId}
- **Purpose**: Soft delete a review (user can only delete their own reviews)
- **Authentication**: Required (user from JWT token)
- **Request**: Path parameter reviewId (Long)
- **Response**: ApiResponse<null>
  ```json
  {
    "isSuccess": true,
    "code": "200",
    "message": "OK",
    "result": null
  }
  ```

### Content APIs

#### Get Content Basic Info
- **Method**: GET
- **Path**: /api/v1/contents/{contentId}
- **Purpose**: Retrieve basic information about a content (movie/TV show)
- **Authentication**: Required

#### Get Content Credits
- **Method**: GET
- **Path**: /api/v1/contents/{contentId}/credits
- **Purpose**: Retrieve cast and crew information
- **Authentication**: Required

#### Get Content Seasons
- **Method**: GET
- **Path**: /api/v1/contents/{contentId}/seasons
- **Purpose**: Retrieve season list for TV shows
- **Authentication**: Required

#### Get Season Episodes
- **Method**: GET
- **Path**: /api/v1/contents/{contentId}/seasons/{seasonNo}/episodes
- **Purpose**: Retrieve episode list for a specific season
- **Authentication**: Required

### User APIs

#### User Signup
- **Method**: POST
- **Path**: /api/v1/user/signup
- **Purpose**: Register a new user account
- **Authentication**: Not required

#### User Login
- **Method**: POST
- **Path**: /api/v1/user/login
- **Purpose**: Authenticate user and issue JWT tokens
- **Authentication**: Not required

#### Check User ID Availability
- **Method**: GET
- **Path**: /api/v1/user/check-userid
- **Purpose**: Check if user ID is available
- **Authentication**: Not required

#### Check Email Availability
- **Method**: GET
- **Path**: /api/v1/user/check-email
- **Purpose**: Check if email is available
- **Authentication**: Not required

#### Refresh Token
- **Method**: POST
- **Path**: /api/v1/user/refresh
- **Purpose**: Refresh JWT access token using refresh token
- **Authentication**: Not required (uses refresh token from cookie)

#### Get Current User
- **Method**: GET
- **Path**: /api/v1/user/me
- **Purpose**: Get current authenticated user information
- **Authentication**: Not required (public endpoint)

#### User Logout
- **Method**: POST
- **Path**: /api/v1/user/logout
- **Purpose**: Logout user and invalidate tokens
- **Authentication**: Required

### Admin APIs

#### Content Administration
- **Path**: /api/v1/admin/contents/*
- **Purpose**: Administrative operations for content management
- **Authentication**: Required (admin role)

## Internal APIs

### ReviewCommandService
- **Methods**:
  - `createReview(Long contentId, String userId, ReviewRequest.Create request)` → ReviewResponse.Created
  - `deleteReview(Long reviewId, String userId)` → void
- **Purpose**: Handle review write operations
- **Validation**: User ownership validation for delete operations

### ReviewQueryService
- **Methods**:
  - `getReview(Long reviewId)` → ReviewResponse.Review
  - `getReviewList(Long contentId, ReviewRequest.Slice request)` → ReviewResponse.SliceList
- **Purpose**: Handle review read operations
- **Features**: Cursor-based pagination for efficient large dataset handling

### ReviewDao (MyBatis Interface)
- **Methods**:
  - `insertReview(ReviewVo vo)` → int (returns generated reviewId)
  - `selectReview(Long reviewId)` → ReviewVo
  - `selectReviewList(Long contentId)` → List<ReviewVo>
  - `selectReviewCursor(Long contentId, LocalDateTime cursorCreatedAt, Long cursorReviewId, Integer limit)` → List<ReviewVo>
  - `deleteReview(Long reviewId, String userId)` → int (soft delete)
- **Purpose**: Data access layer for review operations

### ReviewLikeDao (MyBatis Interface) - TARGET FOR IMPLEMENTATION
- **Methods** (defined in ReviewLikeMapper.xml):
  - `insertReviewLike(ReviewLikeVo vo)` → int (returns generated reviewLikeId)
  - `deleteReviewLike(String userId, Long reviewId)` → int
  - `existsReviewLike(String userId, Long reviewId)` → int (count)
  - `countReviewLikes(Long reviewId)` → int
  - `selectReviewLike(String userId, Long reviewId)` → ReviewLikeVo
- **Purpose**: Data access layer for review like operations
- **Status**: Interface needs to be implemented (XML mapper already complete)

### JwtTokenProvider
- **Methods**:
  - `generateAccessToken(String userId)` → String
  - `generateRefreshToken(String userId)` → JwtToken
  - `validateToken(String token)` → boolean
  - `getUserIdFromToken(String token)` → String
- **Purpose**: JWT token generation and validation

### CustomerOauth2UserService
- **Methods**:
  - `loadUser(OAuth2UserRequest userRequest)` → OAuth2User
- **Purpose**: Load user details from OAuth2 provider (Google)

## Data Models

### ReviewVo
- **Fields**:
  - reviewId (Long) - Primary key
  - contentId (Long) - Foreign key to CONTENT
  - userId (String) - Foreign key to USERS
  - reviewBody (String) - Review text content
  - startTime (Integer) - YouTube video start time (seconds)
  - endTime (Integer) - YouTube video end time (seconds)
  - youtubeId (String) - YouTube video ID
  - isSpoiler (Boolean) - Spoiler flag
  - trackId (String) - Music track ID
  - delYn (String) - Soft delete flag ('Y'/'N')
  - deletedAt (LocalDateTime) - Deletion timestamp
  - createdAt (LocalDateTime) - Creation timestamp (inherited from BaseVo)
  - updatedAt (LocalDateTime) - Update timestamp (inherited from BaseVo)
- **Relationships**: 
  - Belongs to CONTENT (contentId)
  - Belongs to USERS (userId)
  - Has many REVIEW_LIKE (reviewId)
- **Validation**: 
  - reviewBody is required
  - isSpoiler is required
  - delYn must be 'Y' or 'N'

### ReviewLikeVo - TARGET FOR IMPLEMENTATION
- **Fields** (based on database schema and mapper):
  - reviewLikeId (Long) - Primary key
  - userId (String) - Foreign key to USERS
  - reviewId (Long) - Foreign key to REVIEW
  - createdAt (LocalDateTime) - Creation timestamp
  - updatedAt (LocalDateTime) - Update timestamp
- **Relationships**:
  - Belongs to USERS (userId)
  - Belongs to REVIEW (reviewId)
- **Validation**:
  - Unique constraint on (userId, reviewId) - one like per user per review

### ReviewRequest.Create
- **Fields**:
  - reviewBody (String, required) - Review text
  - isSpoiler (Boolean, required) - Spoiler flag
  - trackId (String, optional) - Music track ID
  - youtubeId (String, optional) - YouTube video ID
  - startTime (Integer, optional) - Video start time
  - endTime (Integer, optional) - Video end time
- **Validation**: 
  - reviewBody: @NotBlank
  - isSpoiler: @NotNull

### ReviewRequest.Slice
- **Fields**:
  - size (Integer, default: 10) - Page size
  - cursorCreatedAt (LocalDateTime, optional) - Cursor timestamp
  - cursorReviewId (Long, optional) - Cursor review ID
- **Validation**:
  - size: @Positive (must be >= 1)

### ReviewResponse.Review
- **Fields**:
  - reviewId (Long)
  - contentId (Long)
  - userId (String)
  - reviewBody (String)
  - startTime (Integer)
  - endTime (Integer)
  - youtubeId (String)
  - isSpoiler (Boolean)
  - trackId (String)

### ReviewResponse.SliceList
- **Fields**:
  - reviewList (List<Review>) - Current page reviews
  - nextCursor (Cursor) - Cursor for next page
  - hasNext (Boolean) - Whether more pages exist

### ApiResponse<T>
- **Fields**:
  - isSuccess (Boolean) - Success flag
  - code (String) - Response code
  - message (String) - Response message
  - result (T) - Response data (nullable)
- **Purpose**: Standard wrapper for all API responses

## Authentication & Authorization

### JWT Token Structure
- **Access Token**: Short-lived token for API authentication
- **Refresh Token**: Long-lived token for access token renewal
- **Storage**: HTTP-only cookies
- **Claims**: userId, expiration, JTI (for refresh tokens)

### Security Endpoints
- **Public**: /api/v1/user/signup, /api/v1/user/login, /api/v1/user/refresh, /api/v1/user/me, Swagger UI
- **Authenticated**: All other endpoints require valid JWT token
- **Admin**: /api/v1/admin/* requires admin role

### CORS Configuration
- **Allowed Origins**: http://localhost:5173 (development frontend)
- **Allowed Methods**: GET, POST, PUT, PATCH, DELETE
- **Credentials**: Allowed (for cookies)

