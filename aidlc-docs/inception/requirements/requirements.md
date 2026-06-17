# Requirements Document - ReviewLike Feature

## Intent Analysis Summary

### User Request
Implement ReviewLike (리뷰 추천) feature for the ScenePick backend application.

### Request Type
**New Feature** - Adding social engagement functionality to existing review system

### Scope Estimate
**Single Component** - Changes primarily within the review package with minor modifications to existing Review response DTOs

### Complexity Estimate
**Simple** - Clear implementation path following existing Review patterns, database schema and SQL queries already complete

### Implementation Context
- **Project Type**: Brownfield (existing Spring Boot application)
- **Foundation Status**: Database table (REVIEW_LIKE) exists, MyBatis mapper (ReviewLikeMapper.xml) complete
- **Empty Files**: ReviewLikeVo.java, ReviewLikeDao.java need implementation
- **Pattern Reference**: Existing Review implementation provides clear pattern to follow

---

## Functional Requirements

### FR1: Review Like Toggle
**Priority**: High  
**Description**: Users can like or unlike a review through a single toggle endpoint

**Acceptance Criteria**:
- POST /api/v1/reviews/{reviewId}/like endpoint toggles like status
- If user hasn't liked the review → add like (insert REVIEW_LIKE record)
- If user has already liked the review → remove like (delete REVIEW_LIKE record)
- Operation is idempotent and atomic
- Returns updated like status and count

**Business Rules**:
- Users cannot like their own reviews (validation required)
- Authentication required for like/unlike operations
- Review must exist and not be soft-deleted (DEL_YN='N')

### FR2: Like Count Display
**Priority**: High  
**Description**: Display like count with every review in API responses

**Acceptance Criteria**:
- Add `likeCount` field to ReviewResponse.Review DTO
- Like count included in all review responses (single review, review list, cursor pagination)
- Count reflects current number of likes from REVIEW_LIKE table
- Count is always accurate and up-to-date

### FR3: User Like Status Display
**Priority**: High  
**Description**: Show whether the current user has liked each review

**Acceptance Criteria**:
- Add `isLikedByCurrentUser` field to ReviewResponse.Review DTO
- Field is populated only when user is authenticated
- Field is null or false when user is not authenticated
- Status reflects current user's like state from REVIEW_LIKE table

### FR4: Public Like Count Access
**Priority**: Medium  
**Description**: Like counts are visible to all users (authenticated and unauthenticated)

**Acceptance Criteria**:
- Like count visible in all review API responses
- No authentication required to view like counts
- Consistent with existing public review viewing behavior

### FR5: Cascade Delete on Review Deletion
**Priority**: Medium  
**Description**: When a review is soft-deleted, all associated likes are cascade deleted

**Acceptance Criteria**:
- Database foreign key constraint handles cascade delete (FK_REVIEW_LIKE_TO_REVIEW ON DELETE CASCADE)
- When review DEL_YN is set to 'Y', likes remain in database but are inaccessible
- When review is hard-deleted (if ever implemented), likes are automatically removed by FK constraint

### FR6: Idempotent Unlike Operation
**Priority**: Low  
**Description**: Attempting to unlike a review that isn't liked succeeds silently

**Acceptance Criteria**:
- If user tries to unlike a review they haven't liked, operation succeeds without error
- No database changes occur
- Returns success response with current state (not liked, count unchanged)

### FR7: Popularity Sorting (Sort by Like Count)
**Priority**: High  
**Description**: Users can view review lists sorted by popularity (like count) in addition to chronological order

**Acceptance Criteria**:
- Add optional `sortBy` query parameter to review list endpoint
- Support two sort options:
  - `LATEST` (default): Sort by creation date descending (existing behavior)
  - `POPULAR`: Sort by like count descending, then by creation date descending (tie-breaker)
- Sorting works with cursor-based pagination
- Cursor includes both timestamp and like count for POPULAR sort
- Performance remains acceptable with proper database indexing

**Business Rules**:
- Default sort order is LATEST if sortBy parameter not provided (backward compatible)
- POPULAR sort shows reviews with most likes first
- Reviews with same like count are sorted by creation date (newest first)
- Cursor pagination must work correctly with both sort orders

---

## Non-Functional Requirements

### NFR1: Performance
**Priority**: High  
**Description**: Like operations and popularity sorting must be fast and efficient

**Acceptance Criteria**:
- Like toggle operation completes in < 200ms (p95)
- Like count query uses database index on REVIEW_ID
- Like status check uses composite index on (USER_ID, REVIEW_ID)
- No N+1 query problems when loading review lists with like counts
- Popularity sorting query completes in < 500ms (p95) for typical page sizes
- Cursor pagination with popularity sort remains efficient

**Technical Approach**:
- Use existing database unique constraint UK_REVIEW_LIKE_USER_REVIEW (USER_ID, REVIEW_ID)
- Leverage MyBatis batch operations if needed for review lists
- Add database index on REVIEW_ID for count queries
- Use SQL subquery or JOIN with COUNT() for popularity sorting
- Consider composite index on (CONTENT_ID, like_count, CREATED_AT) for optimal sort performance

### NFR2: Data Integrity
**Priority**: High  
**Description**: Ensure data consistency and prevent duplicate likes

**Acceptance Criteria**:
- Database unique constraint prevents duplicate likes (UK_REVIEW_LIKE_USER_REVIEW)
- Foreign key constraints ensure referential integrity
- Transactions ensure atomic like/unlike operations
- No orphaned REVIEW_LIKE records

**Technical Approach**:
- Rely on database constraints (already defined in schema)
- Use Spring @Transactional for service methods
- Handle constraint violation exceptions gracefully

### NFR3: Security
**Priority**: High  
**Description**: Protect like operations from unauthorized access and abuse

**Acceptance Criteria**:
- Like/unlike operations require valid JWT authentication
- Users can only like/unlike as themselves (userId from JWT token)
- Users cannot like their own reviews (business rule validation)
- No SQL injection vulnerabilities (MyBatis parameterized queries)

**Technical Approach**:
- Use existing Spring Security JWT authentication
- Extract userId from @AuthenticationPrincipal User
- Validate review ownership before allowing like
- MyBatis #{} syntax for parameterized queries (already used in mapper)

### NFR4: Scalability
**Priority**: Medium  
**Description**: System handles growing number of likes efficiently

**Acceptance Criteria**:
- Database can handle millions of REVIEW_LIKE records
- Like count queries remain fast as data grows
- No performance degradation with high like volume

**Technical Approach**:
- Database indexes on foreign keys (REVIEW_ID, USER_ID)
- Unique constraint serves as composite index
- Oracle database auto-increment for primary key (REVIEW_LIKE_ID)

### NFR5: Maintainability
**Priority**: Medium  
**Description**: Code follows existing patterns and is easy to maintain

**Acceptance Criteria**:
- Follows existing Review package structure and patterns
- Consistent with Command-Query Separation (ReviewLikeCommandService)
- Uses existing infrastructure (ApiResponse, error handling, JWT)
- Comprehensive Swagger/OpenAPI documentation

**Technical Approach**:
- Mirror Review package structure (Controller, Service, DAO, VO, DTOs)
- Use Lombok for boilerplate reduction
- Follow Naver coding conventions (Checkstyle)
- Add @Operation annotations for Swagger

### NFR6: Reliability
**Priority**: Medium  
**Description**: Like operations are reliable and handle errors gracefully

**Acceptance Criteria**:
- Database constraint violations handled with appropriate error messages
- Review not found returns 404 error
- Unauthorized access returns 401 error
- All errors use standard ApiResponse format

**Technical Approach**:
- Use existing ExceptionAdvice for global error handling
- Define custom exceptions if needed (e.g., CannotLikeOwnReviewException)
- Return appropriate HTTP status codes
- Consistent error response format

---

## API Specifications

### Endpoint 1: Toggle Review Like
**Method**: POST  
**Path**: /api/v1/reviews/{reviewId}/like  
**Authentication**: Required (JWT)

**Path Parameters**:
- `reviewId` (Long, required) - ID of the review to like/unlike

**Request Body**: None

**Response**: ApiResponse<ReviewLikeResponse>
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "OK",
  "result": {
    "reviewId": 123,
    "isLiked": true,
    "likeCount": 42
  }
}
```

**Response Fields**:
- `reviewId` (Long) - ID of the review
- `isLiked` (Boolean) - Current like status after toggle
- `likeCount` (Integer) - Updated like count

**Error Responses**:
- 401 Unauthorized - User not authenticated
- 404 Not Found - Review does not exist or is deleted
- 400 Bad Request - User attempting to like their own review

**Business Logic**:
1. Validate user is authenticated (JWT token)
2. Validate review exists and is not deleted
3. Validate user is not the review author
4. Check if user has already liked the review
5. If liked → delete like record (unlike)
6. If not liked → insert like record (like)
7. Return updated status and count

---

### Endpoint 2: Get Review List with Sorting (MODIFIED)
**Method**: GET  
**Path**: /api/v1/contents/{contentId}/reviews  
**Authentication**: Required

**Path Parameters**:
- `contentId` (Long, required) - ID of the content

**Query Parameters**:
- `size` (Integer, optional, default: 10) - Number of reviews per page
- `sortBy` (String, optional, default: "LATEST") - Sort order: "LATEST" or "POPULAR"
- `cursorCreatedAt` (LocalDateTime, optional) - Cursor timestamp from previous page
- `cursorReviewId` (Long, optional) - Cursor review ID from previous page
- `cursorLikeCount` (Integer, optional) - Cursor like count (required for POPULAR sort)

**Sort Options**:
- `LATEST`: Sort by creation date descending (default, existing behavior)
- `POPULAR`: Sort by like count descending, then creation date descending

**Response**: ApiResponse<ReviewResponse.SliceList>
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "OK",
  "result": {
    "reviewList": [
      {
        "reviewId": 123,
        "contentId": 100,
        "userId": "user123",
        "reviewBody": "Great movie!",
        "likeCount": 42,
        "isLikedByCurrentUser": true,
        ...
      }
    ],
    "nextCursor": {
      "createdAt": "2026-02-27T10:30:00",
      "reviewId": 50,
      "likeCount": 15
    },
    "hasNext": true
  }
}
```

**Cursor Behavior**:
- LATEST sort: Uses `cursorCreatedAt` and `cursorReviewId`
- POPULAR sort: Uses `cursorLikeCount`, `cursorCreatedAt`, and `cursorReviewId`

**Business Logic**:
1. Validate content exists
2. Determine sort order from `sortBy` parameter (default: LATEST)
3. If LATEST: Use existing cursor pagination (CREATED_AT DESC, REVIEW_ID DESC)
4. If POPULAR: Use like count cursor pagination (LIKE_COUNT DESC, CREATED_AT DESC, REVIEW_ID DESC)
5. Join with REVIEW_LIKE table to get like counts
6. Populate like count and like status for each review
7. Return paginated results with appropriate cursor

---

## Modified Components

### ReviewRequest.Slice DTO
**Changes**: Add sortBy and cursorLikeCount fields

**New Fields**:
- `sortBy` (String, optional, default: "LATEST") - Sort order: "LATEST" or "POPULAR"
- `cursorLikeCount` (Integer, optional) - Cursor like count for POPULAR sort pagination

**Example**:
```java
@Schema(description = "리뷰 페이징 조회 요청")
public record Slice(
    @Schema(description = "한 번에 가져올 리뷰 개수", defaultValue = "10")
    @Positive
    Integer size,

    @Schema(description = "정렬 방식", defaultValue = "LATEST", allowableValues = {"LATEST", "POPULAR"})
    String sortBy,

    @Schema(description = "이전 페이지의 마지막 리뷰 생성 시각")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime cursorCreatedAt,

    @Schema(description = "이전 페이지의 마지막 리뷰 ID")
    Long cursorReviewId,

    @Schema(description = "이전 페이지의 마지막 리뷰 좋아요 수 (POPULAR 정렬 시 필요)")
    Integer cursorLikeCount
) {
}
```

### ReviewResponse.Review DTO
**Changes**: Add two new fields

**New Fields**:
- `likeCount` (Integer) - Total number of likes for the review
- `isLikedByCurrentUser` (Boolean, nullable) - Whether current user liked the review (null if not authenticated)

**Example**:
```java
@Schema(name = "Review", description = "리뷰 조회 응답")
public record Review(
    @Schema(description = "리뷰 ID")
    Long reviewId,
    
    @Schema(description = "리뷰 작품")
    Long contentId,
    
    @Schema(description = "리뷰 작성자")
    String userId,
    
    @Schema(description = "리뷰 본문")
    String reviewBody,
    
    @Schema(description = "첨부 영상 시작 시간")
    Integer startTime,
    
    @Schema(description = "첨부 영상 종료 시간")
    Integer endTime,
    
    @Schema(description = "첨부 영상 유튜브 ID")
    String youtubeId,
    
    @Schema(description = "리뷰 스포일러 여부")
    Boolean isSpoiler,
    
    @Schema(description = "리뷰 트랙 ID")
    String trackId,
    
    // NEW FIELDS
    @Schema(description = "리뷰 좋아요 수")
    Integer likeCount,
    
    @Schema(description = "현재 사용자의 좋아요 여부")
    Boolean isLikedByCurrentUser
) {
}
```

---

## New Components to Implement

### 1. ReviewLikeVo.java
**Purpose**: Database entity mapping for REVIEW_LIKE table

**Fields**:
- reviewLikeId (Long) - Primary key
- userId (String) - Foreign key to USERS
- reviewId (Long) - Foreign key to REVIEW
- createdAt (LocalDateTime) - Creation timestamp
- updatedAt (LocalDateTime) - Update timestamp

### 2. ReviewLikeDao.java
**Purpose**: Data access interface for ReviewLike operations

**Methods** (already defined in ReviewLikeMapper.xml):
- insertReviewLike(ReviewLikeVo vo) → int
- deleteReviewLike(String userId, Long reviewId) → int
- existsReviewLike(String userId, Long reviewId) → int
- countReviewLikes(Long reviewId) → int
- selectReviewLike(String userId, Long reviewId) → ReviewLikeVo

### 3. ReviewLikeCommandService.java
**Purpose**: Handle like/unlike operations (write operations)

**Methods**:
- toggleReviewLike(Long reviewId, String userId) → ReviewLikeResponse

### 4. ReviewLikeController.java
**Purpose**: REST API endpoint for like operations

**Endpoints**:
- POST /api/v1/reviews/{reviewId}/like

### 5. ReviewLikeResponse.java (DTO)
**Purpose**: Response DTO for like operations

**Fields**:
- reviewId (Long)
- isLiked (Boolean)
- likeCount (Integer)

### 6. Modified: ReviewRequest.Slice (DTO)
**Changes**: Add sorting parameters

**New Fields**:
- sortBy (String) - Sort order: "LATEST" or "POPULAR"
- cursorLikeCount (Integer) - Cursor like count for POPULAR sort

### 7. Modified: ReviewResponse.Cursor (DTO)
**Changes**: Add like count for POPULAR sort cursor

**New Field**:
- likeCount (Integer, optional) - Like count for cursor (used in POPULAR sort)

### 8. Modified: ReviewDao.java
**Changes**: Add method for popularity-sorted review list

**New Method**:
- selectReviewCursorByPopularity(Long contentId, Integer cursorLikeCount, LocalDateTime cursorCreatedAt, Long cursorReviewId, Integer limit) → List<ReviewVo>

### 9. Modified: ReviewMapper.xml
**Changes**: Add SQL query for popularity sorting

**New Query**: selectReviewCursorByPopularity
- JOIN with REVIEW_LIKE to get like counts
- ORDER BY like_count DESC, CREATED_AT DESC, REVIEW_ID DESC
- Cursor-based pagination with like count, timestamp, and ID

### 10. Modified: ReviewQueryService.java
**Changes**: Update to include like count, like status, and support popularity sorting

**New Logic**:
- Query like count for each review
- Query like status for current user (if authenticated)
- Populate new fields in ReviewResponse.Review
- Support LATEST and POPULAR sort orders
- Handle cursor pagination for both sort types

---

## Database Schema (Already Exists)

### REVIEW_LIKE Table
```sql
CREATE TABLE REVIEW_LIKE
(
    REVIEW_LIKE_ID NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    USER_ID        VARCHAR2(50)                     NOT NULL,
    REVIEW_ID      NUMBER                           NOT NULL,
    CREATED_AT     TIMESTAMP   DEFAULT SYSTIMESTAMP NOT NULL,
    UPDATED_AT     TIMESTAMP,

    CONSTRAINT FK_REVIEW_LIKE_TO_USER FOREIGN KEY (USER_ID) 
        REFERENCES USERS (USER_ID) ON DELETE CASCADE,
    CONSTRAINT FK_REVIEW_LIKE_TO_REVIEW FOREIGN KEY (REVIEW_ID) 
        REFERENCES REVIEW (REVIEW_ID) ON DELETE CASCADE,
    CONSTRAINT UK_REVIEW_LIKE_USER_REVIEW UNIQUE (USER_ID, REVIEW_ID)
);
```

**Key Constraints**:
- Primary Key: REVIEW_LIKE_ID (auto-increment)
- Foreign Keys: USER_ID, REVIEW_ID (cascade delete)
- Unique Constraint: (USER_ID, REVIEW_ID) - prevents duplicate likes

---

## Implementation Notes

### Existing Infrastructure to Leverage
- **ApiResponse**: Standard response wrapper (already used)
- **JWT Authentication**: Extract userId from @AuthenticationPrincipal User
- **ExceptionAdvice**: Global exception handling
- **MyBatis Mappers**: SQL queries already complete in ReviewLikeMapper.xml
- **Swagger**: Add @Operation annotations for API documentation

### Pattern to Follow
- **Review Package Structure**: Mirror existing Review implementation
  - Controller → Service (Command/Query) → DAO → MyBatis Mapper
- **DTO Pattern**: Use Java Records for immutable DTOs
- **Error Handling**: Use existing error handling infrastructure
- **Validation**: Use Spring @Valid and custom validators

### Testing Considerations
- Unit tests for service layer logic
- Integration tests for database operations
- API tests for controller endpoints
- Test edge cases: own review, non-existent review, duplicate likes

---

## Success Criteria

### Feature Complete When:
1. ✅ Users can toggle like/unlike on reviews via POST endpoint
2. ✅ Like counts displayed in all review API responses
3. ✅ User like status shown when authenticated
4. ✅ Users cannot like their own reviews (validation)
5. ✅ Like operations require authentication
6. ✅ Likes cascade delete with reviews
7. ✅ Review list supports LATEST and POPULAR sorting
8. ✅ Cursor pagination works correctly with both sort orders
9. ✅ POPULAR sort shows reviews with most likes first
10. ✅ All operations follow existing code patterns
11. ✅ Comprehensive Swagger documentation
12. ✅ Error handling consistent with existing APIs
13. ✅ Code passes Checkstyle validation (Naver conventions)

### Quality Gates:
- All new code follows existing patterns and conventions
- No breaking changes to existing Review APIs
- Database constraints prevent data integrity issues
- Performance meets NFR targets (< 200ms p95)
- Swagger documentation complete and accurate

