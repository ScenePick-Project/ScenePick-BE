# ReviewLike Feature - Code Generation Plan

## Unit Context

### Unit Name
ReviewLike Feature

### Unit Purpose
Implement social engagement functionality allowing users to like/unlike reviews with toggle behavior and view reviews sorted by popularity (like count).

### Stories Implemented
- FR1: Review Like Toggle
- FR2: Like Count Display
- FR3: User Like Status Display
- FR4: Public Like Count Access
- FR5: Cascade Delete on Review Deletion
- FR6: Idempotent Unlike Operation
- FR7: Popularity Sorting (Sort by Like Count)

### Dependencies
- Existing Review package (ReviewDao, ReviewVo, ReviewResponse, ReviewRequest)
- Existing User authentication (JWT, Spring Security)
- Existing Common infrastructure (ApiResponse, ExceptionAdvice)
- Database: REVIEW_LIKE table (already exists)
- MyBatis: ReviewLikeMapper.xml (already exists with SQL queries)

### Expected Interfaces
- POST /api/v1/reviews/{reviewId}/like - Toggle like/unlike
- GET /api/v1/contents/{contentId}/reviews?sortBy=POPULAR - Get reviews sorted by popularity

### Database Entities
- ReviewLikeVo - Maps to REVIEW_LIKE table

### Service Boundaries
- ReviewLikeCommandService - Handle like/unlike operations
- ReviewQueryService (modify) - Populate like data and support sorting

---

## Code Generation Steps

### Step 1: Create ReviewLikeVo (Value Object)
- [x] **File**: `src/main/java/com/project/scenepickbe/review/vo/ReviewLikeVo.java`
- [ ] **Action**: Implement empty file
- [ ] **Content**:
  - Extend BaseVo for createdAt/updatedAt
  - Add @Data and @Alias("ReviewLikeVo") annotations
  - Fields: reviewLikeId (Long), userId (String), reviewId (Long)
  - Follow existing ReviewVo pattern
- [ ] **Story**: FR1, FR2, FR3

### Step 2: Create ReviewLikeDao (Data Access Interface)
- [x] **File**: `src/main/java/com/project/scenepickbe/review/dao/ReviewLikeDao.java`
- [ ] **Action**: Implement empty file
- [ ] **Content**:
  - Interface with @Mapper annotation
  - Methods matching ReviewLikeMapper.xml:
    - insertReviewLike(ReviewLikeVo vo) → int
    - deleteReviewLike(@Param("userId") String userId, @Param("reviewId") Long reviewId) → int
    - existsReviewLike(@Param("userId") String userId, @Param("reviewId") Long reviewId) → int
    - countReviewLikes(@Param("reviewId") Long reviewId) → int
    - selectReviewLike(@Param("userId") String userId, @Param("reviewId") Long reviewId) → ReviewLikeVo
  - Follow existing ReviewDao pattern
- [ ] **Story**: FR1, FR2, FR3

### Step 3: Create ReviewLikeResponse (Response DTO)
- [x] **File**: `src/main/java/com/project/scenepickbe/review/dto/response/ReviewLikeResponse.java`
- [ ] **Action**: Create new file
- [ ] **Content**:
  - Java Record with @Schema annotation
  - Fields: reviewId (Long), isLiked (Boolean), likeCount (Integer)
  - Follow existing ReviewResponse pattern
- [ ] **Story**: FR1

### Step 4: Create ReviewLikeCommandService (Business Logic)
- [x] **File**: `src/main/java/com/project/scenepickbe/review/service/ReviewLikeCommandService.java`
- [ ] **Action**: Create new file
- [ ] **Content**:
  - @Service annotation
  - @RequiredArgsConstructor for dependency injection
  - Dependencies: ReviewLikeDao, ReviewDao
  - Method: toggleReviewLike(Long reviewId, String userId) → ReviewLikeResponse
  - Business logic:
    - Validate review exists and not deleted
    - Validate user is not review author (cannot like own review)
    - Check if already liked (existsReviewLike)
    - If liked: deleteReviewLike (unlike)
    - If not liked: insertReviewLike (like)
    - Get updated like count
    - Return ReviewLikeResponse
  - Use @Transactional for atomicity
  - Follow existing ReviewCommandService pattern
- [ ] **Story**: FR1, FR6

### Step 5: Create ReviewLikeController (REST API)
- [x] **File**: `src/main/java/com/project/scenepickbe/review/controller/ReviewLikeController.java`
- [ ] **Action**: Create new file
- [ ] **Content**:
  - @RestController, @RequestMapping("/api/v1"), @RequiredArgsConstructor
  - @Tag annotation for Swagger
  - Dependency: ReviewLikeCommandService
  - Endpoint: POST /reviews/{reviewId}/like
    - @Operation annotation for Swagger
    - @PathVariable Long reviewId
    - @AuthenticationPrincipal User user
    - Call toggleReviewLike with user.getUsername()
    - Return ApiResponse.onSuccess(result)
  - Follow existing ReviewController pattern
- [ ] **Story**: FR1

### Step 6: Modify ReviewResponse.Review (Add Like Fields)
- [x] **File**: `src/main/java/com/project/scenepickbe/review/dto/response/ReviewResponse.java`
- [ ] **Action**: Modify existing Review record
- [ ] **Content**:
  - Add likeCount (Integer) field with @Schema annotation
  - Add isLikedByCurrentUser (Boolean) field with @Schema annotation
  - Keep all existing fields unchanged
- [ ] **Story**: FR2, FR3, FR4

### Step 7: Modify ReviewRequest.Slice (Add Sort Parameters)
- [x] **File**: `src/main/java/com/project/scenepickbe/review/dto/request/ReviewRequest.java`
- [ ] **Action**: Modify existing Slice record
- [ ] **Content**:
  - Add sortBy (String) field with @Schema annotation (default: "LATEST")
  - Add cursorLikeCount (Integer) field with @Schema annotation
  - Keep all existing fields unchanged
- [ ] **Story**: FR7

### Step 8: Modify ReviewResponse.Cursor (Add Like Count)
- [x] **File**: `src/main/java/com/project/scenepickbe/review/dto/response/ReviewResponse.java`
- [ ] **Action**: Modify existing Cursor record
- [ ] **Content**:
  - Add likeCount (Integer) field with @Schema annotation
  - Keep all existing fields unchanged
- [ ] **Story**: FR7

### Step 9: Modify ReviewDao (Add Popularity Sort Method)
- [x] **File**: `src/main/java/com/project/scenepickbe/review/dao/ReviewDao.java`
- [ ] **Action**: Modify existing interface
- [ ] **Content**:
  - Add method: selectReviewCursorByPopularity(
      @Param("contentId") Long contentId,
      @Param("cursorLikeCount") Integer cursorLikeCount,
      @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
      @Param("cursorReviewId") Long cursorReviewId,
      @Param("limit") Integer limit
    ) → List<ReviewVo>
  - Keep all existing methods unchanged
- [ ] **Story**: FR7

### Step 10: Modify ReviewMapper.xml (Add Popularity Sort Query)
- [x] **File**: `src/main/resources/mybatis/mapper/ReviewMapper.xml`
- [ ] **Action**: Modify existing mapper
- [ ] **Content**:
  - Add selectReviewCursorByPopularity query:
    - SELECT with LEFT JOIN to REVIEW_LIKE
    - COUNT(RL.REVIEW_LIKE_ID) as like_count
    - WHERE contentId and DEL_YN='N'
    - Cursor conditions for (like_count, CREATED_AT, REVIEW_ID)
    - ORDER BY like_count DESC, CREATED_AT DESC, REVIEW_ID DESC
    - FETCH NEXT #{limit} ROWS ONLY
  - Keep all existing queries unchanged
- [ ] **Story**: FR7

### Step 11: Modify ReviewQueryService (Add Like Data Population and Sorting)
- [x] **File**: `src/main/java/com/project/scenepickbe/review/service/ReviewQueryService.java`
- [ ] **Action**: Modify existing service
- [ ] **Content**:
  - Add dependency: ReviewLikeDao
  - Modify getReview method:
    - After fetching review, get like count (countReviewLikes)
    - If user authenticated, get like status (existsReviewLike)
    - Populate likeCount and isLikedByCurrentUser in response
  - Modify getReviewList method:
    - Check sortBy parameter (default: "LATEST")
    - If "LATEST": Use existing selectReviewCursor
    - If "POPULAR": Use new selectReviewCursorByPopularity
    - For each review, populate like count and like status
    - Build cursor with appropriate fields (add likeCount for POPULAR)
  - Keep all existing logic unchanged
- [ ] **Story**: FR2, FR3, FR4, FR7

### Step 12: Create Code Summary Documentation
- [x] **File**: `aidlc-docs/construction/reviewlike/code/implementation-summary.md`
- [ ] **Action**: Create documentation
- [ ] **Content**:
  - List all created files with paths
  - List all modified files with changes
  - Explain like toggle logic
  - Explain popularity sorting logic
  - Document API endpoints
  - Include code snippets for key methods
- [ ] **Story**: All

---

## Story Traceability

### FR1: Review Like Toggle
- Step 1: ReviewLikeVo
- Step 2: ReviewLikeDao
- Step 3: ReviewLikeResponse
- Step 4: ReviewLikeCommandService (toggle logic)
- Step 5: ReviewLikeController (POST endpoint)

### FR2: Like Count Display
- Step 1: ReviewLikeVo
- Step 2: ReviewLikeDao (countReviewLikes)
- Step 6: ReviewResponse.Review (likeCount field)
- Step 11: ReviewQueryService (populate like count)

### FR3: User Like Status Display
- Step 1: ReviewLikeVo
- Step 2: ReviewLikeDao (existsReviewLike)
- Step 6: ReviewResponse.Review (isLikedByCurrentUser field)
- Step 11: ReviewQueryService (populate like status)

### FR4: Public Like Count Access
- Step 11: ReviewQueryService (no authentication check for like count)

### FR5: Cascade Delete on Review Deletion
- Database constraint (already exists in schema)
- No code changes needed

### FR6: Idempotent Unlike Operation
- Step 4: ReviewLikeCommandService (handle non-existent like gracefully)

### FR7: Popularity Sorting
- Step 7: ReviewRequest.Slice (sortBy, cursorLikeCount)
- Step 8: ReviewResponse.Cursor (likeCount)
- Step 9: ReviewDao (selectReviewCursorByPopularity)
- Step 10: ReviewMapper.xml (popularity sort query)
- Step 11: ReviewQueryService (sort logic)

---

## Code Location
- **Workspace Root**: Project root directory
- **Application Code**: `src/main/java/com/project/scenepickbe/`
- **MyBatis Mappers**: `src/main/resources/mybatis/mapper/`
- **Documentation**: `aidlc-docs/construction/reviewlike/code/`

## Total Steps: 12

## Estimated Scope
- New files: 5 (ReviewLikeVo, ReviewLikeDao, ReviewLikeResponse, ReviewLikeCommandService, ReviewLikeController)
- Modified files: 6 (ReviewResponse, ReviewRequest, ReviewDao, ReviewMapper.xml, ReviewQueryService)
- Documentation: 1 (implementation-summary.md)

