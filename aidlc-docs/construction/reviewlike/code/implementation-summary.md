# ReviewLike Feature - Implementation Summary

## Overview
Implemented social engagement functionality allowing users to like/unlike reviews with toggle behavior and view reviews sorted by popularity (like count).

---

## Created Files (5)

### 1. ReviewLikeVo.java
**Path**: `src/main/java/com/project/scenepickbe/review/vo/ReviewLikeVo.java`  
**Purpose**: Database entity mapping for REVIEW_LIKE table

**Fields**:
- `reviewLikeId` (Long) - Primary key
- `userId` (String) - Foreign key to USERS
- `reviewId` (Long) - Foreign key to REVIEW
- `createdAt`, `updatedAt` (LocalDateTime) - Inherited from BaseVo

### 2. ReviewLikeDao.java
**Path**: `src/main/java/com/project/scenepickbe/review/dao/ReviewLikeDao.java`  
**Purpose**: Data access interface for ReviewLike operations

**Methods**:
- `insertReviewLike(ReviewLikeVo vo)` - Add like
- `deleteReviewLike(String userId, Long reviewId)` - Remove like
- `existsReviewLike(String userId, Long reviewId)` - Check if liked
- `countReviewLikes(Long reviewId)` - Get like count
- `selectReviewLike(String userId, Long reviewId)` - Get like info

### 3. ReviewLikeResponse.java
**Path**: `src/main/java/com/project/scenepickbe/review/dto/response/ReviewLikeResponse.java`  
**Purpose**: Response DTO for like operations

**Record**: Toggle
- `reviewId` (Long) - Review ID
- `isLiked` (Boolean) - Current like status
- `likeCount` (Integer) - Updated like count

### 4. ReviewLikeCommandService.java
**Path**: `src/main/java/com/project/scenepickbe/review/service/ReviewLikeCommandService.java`  
**Purpose**: Business logic for like/unlike operations

**Method**: `toggleReviewLike(Long reviewId, String userId)`
**Logic**:
1. Validate review exists and not deleted
2. Prevent liking own review
3. Check if already liked
4. If liked → delete (unlike)
5. If not liked → insert (like)
6. Return updated status and count

**Annotations**: @Service, @Transactional

### 5. ReviewLikeController.java
**Path**: `src/main/java/com/project/scenepickbe/review/controller/ReviewLikeController.java`  
**Purpose**: REST API endpoint for like operations

**Endpoint**: POST /api/v1/reviews/{reviewId}/like
- Toggles like/unlike for authenticated user
- Returns ReviewLikeResponse.Toggle
- Uses @AuthenticationPrincipal for user identification

---

## Modified Files (6)

### 1. ReviewResponse.java
**Path**: `src/main/java/com/project/scenepickbe/review/dto/response/ReviewResponse.java`

**Changes to Review record**:
- Added `likeCount` (Integer) - Total likes for review
- Added `isLikedByCurrentUser` (Boolean) - Current user's like status

**Changes to Cursor record**:
- Added `likeCount` (Integer) - For POPULAR sort pagination

### 2. ReviewRequest.java
**Path**: `src/main/java/com/project/scenepickbe/review/dto/request/ReviewRequest.java`

**Changes to Slice record**:
- Added `sortBy` (String) - Sort order: "LATEST" or "POPULAR"
- Added `cursorLikeCount` (Integer) - Cursor for POPULAR sort

### 3. ReviewDao.java
**Path**: `src/main/java/com/project/scenepickbe/review/dao/ReviewDao.java`

**Added method**:
- `selectReviewCursorByPopularity(...)` - Query reviews sorted by like count

### 4. ReviewMapper.xml
**Path**: `src/main/resources/mybatis/mapper/ReviewMapper.xml`

**Added query**: `selectReviewCursorByPopularity`
```sql
SELECT R.*, COUNT(RL.REVIEW_LIKE_ID) as LIKE_COUNT
FROM REVIEW R
LEFT JOIN REVIEW_LIKE RL ON R.REVIEW_ID = RL.REVIEW_ID
WHERE R.CONTENT_ID = #{contentId} AND R.DEL_YN = 'N'
GROUP BY R.REVIEW_ID, ...
ORDER BY COUNT(RL.REVIEW_LIKE_ID) DESC, R.CREATED_AT DESC, R.REVIEW_ID DESC
```

### 5. ReviewQueryService.java
**Path**: `src/main/java/com/project/scenepickbe/review/service/ReviewQueryService.java`

**Added dependency**: ReviewLikeDao

**Modified methods**:
- `getReview()` - Now enriches review with like data
- `getReviewList()` - Supports LATEST and POPULAR sorting

**New private methods**:
- `enrichReviewWithLikeData()` - Populates likeCount and isLikedByCurrentUser
- `getCurrentUserId()` - Gets authenticated user ID from SecurityContext

**Sorting logic**:
- Default: LATEST (creation date descending)
- POPULAR: Like count descending, then creation date descending
- Cursor includes likeCount for POPULAR sort

---

## API Endpoints

### POST /api/v1/reviews/{reviewId}/like
**Description**: Toggle like/unlike on a review

**Authentication**: Required (JWT)

**Request**:
- Path parameter: `reviewId` (Long)

**Response**:
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

**Business Rules**:
- Users cannot like their own reviews (400 Bad Request)
- Review must exist and not be deleted (404 Not Found)
- Toggle behavior: like if not liked, unlike if already liked

### GET /api/v1/contents/{contentId}/reviews (MODIFIED)
**Description**: Get review list with optional popularity sorting

**Authentication**: Required

**New Query Parameters**:
- `sortBy` (String, optional) - "LATEST" (default) or "POPULAR"
- `cursorLikeCount` (Integer, optional) - For POPULAR sort pagination

**Response Changes**:
- Each review now includes `likeCount` and `isLikedByCurrentUser`
- Cursor includes `likeCount` for POPULAR sort

**Example**:
```
GET /api/v1/contents/100/reviews?sortBy=POPULAR&size=10
```

---

## Key Implementation Details

### Like Toggle Logic
1. Check review exists (ReviewDao.selectReview)
2. Validate not own review (userId comparison)
3. Check if already liked (ReviewLikeDao.existsReviewLike)
4. Toggle: insert or delete like record
5. Get updated count (ReviewLikeDao.countReviewLikes)
6. Return result

### Popularity Sorting
- Uses LEFT JOIN with REVIEW_LIKE table
- COUNT(RL.REVIEW_LIKE_ID) for like count
- ORDER BY like_count DESC, created_at DESC, review_id DESC
- Cursor includes (likeCount, createdAt, reviewId) for stable pagination

### Like Data Enrichment
- For each review, query like count (countReviewLikes)
- If user authenticated, query like status (existsReviewLike)
- Populate likeCount and isLikedByCurrentUser fields
- Null isLikedByCurrentUser for unauthenticated users

### Authentication Handling
- Like/unlike requires authentication (@AuthenticationPrincipal)
- Like count visible to all users (public)
- Like status only populated for authenticated users
- Uses SecurityContextHolder for current user in query service

---

## Database Integration

### Existing Schema
- REVIEW_LIKE table already exists
- Foreign keys with CASCADE DELETE
- Unique constraint on (USER_ID, REVIEW_ID)

### Existing MyBatis Mapper
- ReviewLikeMapper.xml already complete with all SQL queries
- No changes needed to ReviewLikeMapper.xml

### New SQL Query
- Added selectReviewCursorByPopularity to ReviewMapper.xml
- Supports cursor-based pagination with like count

---

## Testing Considerations

### Unit Tests Needed
- ReviewLikeCommandService.toggleReviewLike()
  - Test like (not previously liked)
  - Test unlike (already liked)
  - Test own review validation
  - Test non-existent review
- ReviewQueryService enrichment
  - Test like count population
  - Test like status for authenticated user
  - Test like status null for unauthenticated
- ReviewQueryService sorting
  - Test LATEST sort (default)
  - Test POPULAR sort
  - Test cursor pagination for both sorts

### Integration Tests Needed
- POST /api/v1/reviews/{reviewId}/like
  - Test toggle behavior
  - Test authentication required
  - Test own review prevention
- GET /api/v1/contents/{contentId}/reviews
  - Test sortBy=LATEST
  - Test sortBy=POPULAR
  - Test like data in responses

---

## Functional Requirements Coverage

### FR1: Review Like Toggle ✅
- Implemented in ReviewLikeCommandService.toggleReviewLike()
- Exposed via ReviewLikeController POST endpoint

### FR2: Like Count Display ✅
- Implemented in ReviewQueryService.enrichReviewWithLikeData()
- Added to ReviewResponse.Review DTO

### FR3: User Like Status Display ✅
- Implemented in ReviewQueryService.enrichReviewWithLikeData()
- Added to ReviewResponse.Review DTO
- Only populated for authenticated users

### FR4: Public Like Count Access ✅
- Like count visible in all review responses
- No authentication check for like count

### FR5: Cascade Delete on Review Deletion ✅
- Handled by database FK constraint (ON DELETE CASCADE)
- No code changes needed

### FR6: Idempotent Unlike Operation ✅
- Implemented in ReviewLikeCommandService.toggleReviewLike()
- Silently succeeds if not previously liked

### FR7: Popularity Sorting ✅
- Implemented in ReviewQueryService.getReviewList()
- Added selectReviewCursorByPopularity query
- Supports cursor pagination with like count

---

## Next Steps

1. **Build and Test**: Compile code and run tests
2. **Manual Testing**: Test API endpoints with Swagger UI
3. **Performance Testing**: Verify popularity sort query performance
4. **Code Review**: Review for code quality and patterns
5. **Documentation**: Update API documentation if needed

