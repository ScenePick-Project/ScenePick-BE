# Integration Test Instructions

## Purpose
Test interactions between ReviewLike feature components and existing Review system to ensure they work together correctly with the actual database.

## Test Environment Setup

### 1. Start Required Services

```bash
# Ensure Oracle database is running
# Connection details should be in application.properties or application.yml

# Start Spring Boot application
./gradlew bootRun

# Application should start on http://localhost:8080
# Wait for "Started ScenePickBeApplication" message
```

### 2. Verify Database Schema

```sql
-- Connect to Oracle database and verify tables exist
SELECT table_name FROM user_tables WHERE table_name IN ('REVIEW', 'REVIEW_LIKE', 'USERS', 'CONTENT');

-- Verify REVIEW_LIKE table structure
DESC REVIEW_LIKE;

-- Expected columns:
-- REVIEW_LIKE_ID (NUMBER, PK)
-- USER_ID (VARCHAR2(50), FK)
-- REVIEW_ID (NUMBER, FK)
-- CREATED_AT (TIMESTAMP)
-- UPDATED_AT (TIMESTAMP)
```

### 3. Prepare Test Data

```sql
-- Insert test user (if not exists)
INSERT INTO USERS (USER_ID, EMAIL, USERNAME, PASSWORD, ROLE, DEL_YN)
VALUES ('testuser1', 'test1@example.com', 'TestUser1', 'hashedpassword', 'USER', 'N');

INSERT INTO USERS (USER_ID, EMAIL, USERNAME, PASSWORD, ROLE, DEL_YN)
VALUES ('testuser2', 'test2@example.com', 'TestUser2', 'hashedpassword', 'USER', 'N');

-- Insert test content (if not exists)
INSERT INTO CONTENT (CONTENT_ID, TMDB_ID, CONTENT_TYPE, TITLE, CREATED_AT)
VALUES (1, 12345, 'MOVIE', 'Test Movie', SYSTIMESTAMP);

-- Insert test reviews
INSERT INTO REVIEW (REVIEW_ID, CONTENT_ID, USER_ID, REVIEW_BODY, IS_SPOILER, DEL_YN, CREATED_AT)
VALUES (1, 1, 'testuser1', 'Great movie!', 0, 'N', SYSTIMESTAMP);

INSERT INTO REVIEW (REVIEW_ID, CONTENT_ID, USER_ID, REVIEW_BODY, IS_SPOILER, DEL_YN, CREATED_AT)
VALUES (2, 1, 'testuser2', 'Amazing cinematography!', 0, 'N', SYSTIMESTAMP);

COMMIT;
```

## Integration Test Scenarios

### Scenario 1: ReviewLike Toggle Integration

**Description**: Test complete like/unlike flow with database persistence

**Test Steps**:
1. Authenticate as testuser2
2. Like testuser1's review (reviewId=1)
3. Verify like record created in REVIEW_LIKE table
4. Verify like count increased
5. Unlike the same review
6. Verify like record deleted from REVIEW_LIKE table
7. Verify like count decreased

**API Calls**:
```bash
# 1. Login to get JWT token
curl -X POST http://localhost:8080/api/v1/user/login \
  -H "Content-Type: application/json" \
  -d '{"userId":"testuser2","password":"password"}'

# Save token from response

# 2. Like review
curl -X POST http://localhost:8080/api/v1/reviews/1/like \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json"

# Expected response:
# {
#   "isSuccess": true,
#   "code": "200",
#   "message": "OK",
#   "result": {
#     "reviewId": 1,
#     "isLiked": true,
#     "likeCount": 1
#   }
# }

# 3. Verify in database
# SELECT * FROM REVIEW_LIKE WHERE USER_ID='testuser2' AND REVIEW_ID=1;
# Should return 1 row

# 4. Unlike review (toggle again)
curl -X POST http://localhost:8080/api/v1/reviews/1/like \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json"

# Expected response:
# {
#   "isSuccess": true,
#   "code": "200",
#   "message": "OK",
#   "result": {
#     "reviewId": 1,
#     "isLiked": false,
#     "likeCount": 0
#   }
# }

# 5. Verify in database
# SELECT * FROM REVIEW_LIKE WHERE USER_ID='testuser2' AND REVIEW_ID=1;
# Should return 0 rows
```

**Expected Results**:
- ✅ Like record created/deleted in database
- ✅ Like count accurate
- ✅ Toggle behavior works correctly
- ✅ Transaction commits successfully

**Cleanup**:
```sql
DELETE FROM REVIEW_LIKE WHERE USER_ID='testuser2' AND REVIEW_ID=1;
COMMIT;
```

---

### Scenario 2: Review List with Like Data Integration

**Description**: Test review list API returns like count and like status correctly

**Test Steps**:
1. Create multiple likes on different reviews
2. Fetch review list as authenticated user
3. Verify each review includes likeCount
4. Verify isLikedByCurrentUser is correct for each review
5. Fetch review list as unauthenticated user
6. Verify isLikedByCurrentUser is null

**API Calls**:
```bash
# 1. Setup: Create likes
curl -X POST http://localhost:8080/api/v1/reviews/1/like \
  -H "Authorization: Bearer {token_user2}"

curl -X POST http://localhost:8080/api/v1/reviews/2/like \
  -H "Authorization: Bearer {token_user1}"

# 2. Get review list as testuser2 (authenticated)
curl -X GET "http://localhost:8080/api/v1/contents/1/reviews?size=10" \
  -H "Authorization: Bearer {token_user2}"

# Expected response includes:
# - Review 1: likeCount=1, isLikedByCurrentUser=true (user2 liked it)
# - Review 2: likeCount=1, isLikedByCurrentUser=false (user1 liked it, not user2)

# 3. Get review list without authentication
curl -X GET "http://localhost:8080/api/v1/contents/1/reviews?size=10"

# Expected response includes:
# - Review 1: likeCount=1, isLikedByCurrentUser=null
# - Review 2: likeCount=1, isLikedByCurrentUser=null
```

**Expected Results**:
- ✅ Like counts accurate for all reviews
- ✅ isLikedByCurrentUser correct for authenticated user
- ✅ isLikedByCurrentUser null for unauthenticated user
- ✅ No N+1 query issues (check logs)

---

### Scenario 3: Popularity Sorting Integration

**Description**: Test POPULAR sort returns reviews ordered by like count

**Test Steps**:
1. Create reviews with different like counts
2. Fetch review list with sortBy=POPULAR
3. Verify reviews sorted by like count descending
4. Verify reviews with same like count sorted by creation date
5. Test cursor pagination with POPULAR sort

**Setup**:
```sql
-- Create reviews with varying like counts
-- Review 1: 5 likes
-- Review 2: 3 likes
-- Review 3: 3 likes (created after Review 2)
-- Review 4: 1 like

-- Insert likes for review 1
INSERT INTO REVIEW_LIKE (USER_ID, REVIEW_ID, CREATED_AT)
VALUES ('user1', 1, SYSTIMESTAMP);
-- Repeat for 4 more users...

-- Insert likes for review 2
INSERT INTO REVIEW_LIKE (USER_ID, REVIEW_ID, CREATED_AT)
VALUES ('user1', 2, SYSTIMESTAMP);
-- Repeat for 2 more users...

COMMIT;
```

**API Calls**:
```bash
# 1. Get review list with POPULAR sort
curl -X GET "http://localhost:8080/api/v1/contents/1/reviews?sortBy=POPULAR&size=10" \
  -H "Authorization: Bearer {token}"

# Expected order:
# 1. Review 1 (likeCount=5)
# 2. Review 3 (likeCount=3, newer)
# 3. Review 2 (likeCount=3, older)
# 4. Review 4 (likeCount=1)

# 2. Test cursor pagination
curl -X GET "http://localhost:8080/api/v1/contents/1/reviews?sortBy=POPULAR&size=2&cursorLikeCount=3&cursorCreatedAt=2026-02-27T10:00:00&cursorReviewId=3" \
  -H "Authorization: Bearer {token}"

# Should return reviews after cursor (Review 2, Review 4)
```

**Expected Results**:
- ✅ Reviews sorted by like count descending
- ✅ Tie-breaker by creation date works
- ✅ Cursor pagination works with POPULAR sort
- ✅ Query performance acceptable (< 500ms)

---

### Scenario 4: Business Rule Validation Integration

**Description**: Test business rules are enforced at database and application level

**Test Steps**:
1. Try to like own review (should fail)
2. Try to like non-existent review (should fail)
3. Try to like without authentication (should fail)
4. Try to create duplicate like (should be idempotent)

**API Calls**:
```bash
# 1. Try to like own review
curl -X POST http://localhost:8080/api/v1/reviews/1/like \
  -H "Authorization: Bearer {token_user1}"  # user1 owns review 1

# Expected: 400 Bad Request

# 2. Try to like non-existent review
curl -X POST http://localhost:8080/api/v1/reviews/99999/like \
  -H "Authorization: Bearer {token_user2}"

# Expected: 404 Not Found

# 3. Try to like without authentication
curl -X POST http://localhost:8080/api/v1/reviews/1/like

# Expected: 401 Unauthorized

# 4. Try to create duplicate like (database constraint test)
# Like review twice in rapid succession
curl -X POST http://localhost:8080/api/v1/reviews/1/like \
  -H "Authorization: Bearer {token_user2}"
# Should succeed (isLiked=true)

curl -X POST http://localhost:8080/api/v1/reviews/1/like \
  -H "Authorization: Bearer {token_user2}"
# Should succeed (isLiked=false, toggle behavior)
```

**Expected Results**:
- ✅ Own review like prevented (400)
- ✅ Non-existent review handled (404)
- ✅ Authentication required (401)
- ✅ Toggle behavior prevents duplicates
- ✅ Database unique constraint works

---

### Scenario 5: Cascade Delete Integration

**Description**: Test likes are deleted when review is deleted

**Test Steps**:
1. Create review with likes
2. Soft delete review
3. Verify likes remain in database (soft delete doesn't cascade)
4. Hard delete review (if applicable)
5. Verify likes cascade deleted

**SQL Test**:
```sql
-- 1. Create review and like
INSERT INTO REVIEW (REVIEW_ID, CONTENT_ID, USER_ID, REVIEW_BODY, IS_SPOILER, DEL_YN, CREATED_AT)
VALUES (999, 1, 'testuser1', 'Test review', 0, 'N', SYSTIMESTAMP);

INSERT INTO REVIEW_LIKE (USER_ID, REVIEW_ID, CREATED_AT)
VALUES ('testuser2', 999, SYSTIMESTAMP);

COMMIT;

-- 2. Soft delete review
UPDATE REVIEW SET DEL_YN='Y', DELETED_AT=SYSTIMESTAMP WHERE REVIEW_ID=999;
COMMIT;

-- 3. Verify like still exists
SELECT * FROM REVIEW_LIKE WHERE REVIEW_ID=999;
-- Should return 1 row (soft delete doesn't cascade)

-- 4. Hard delete review (triggers FK cascade)
DELETE FROM REVIEW WHERE REVIEW_ID=999;
COMMIT;

-- 5. Verify like cascade deleted
SELECT * FROM REVIEW_LIKE WHERE REVIEW_ID=999;
-- Should return 0 rows (FK cascade delete worked)
```

**Expected Results**:
- ✅ Soft delete doesn't cascade to likes
- ✅ Hard delete cascades to likes (FK constraint)
- ✅ No orphaned like records

---

## Performance Testing

### Test Query Performance

```sql
-- Test popularity sort query performance
SET TIMING ON;

SELECT R.REVIEW_ID,
       R.USER_ID,
       R.CONTENT_ID,
       R.REVIEW_BODY,
       COUNT(RL.REVIEW_LIKE_ID) as LIKE_COUNT
FROM REVIEW R
LEFT JOIN REVIEW_LIKE RL ON R.REVIEW_ID = RL.REVIEW_ID
WHERE R.CONTENT_ID = 1
  AND R.DEL_YN = 'N'
GROUP BY R.REVIEW_ID, R.USER_ID, R.CONTENT_ID, R.REVIEW_BODY
ORDER BY COUNT(RL.REVIEW_LIKE_ID) DESC, R.CREATED_AT DESC
FETCH NEXT 10 ROWS ONLY;

-- Expected: < 500ms for typical dataset
```

### Load Testing (Optional)

```bash
# Use Apache Bench or similar tool
ab -n 1000 -c 10 -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/v1/contents/1/reviews?sortBy=POPULAR

# Monitor:
# - Response times (should be < 500ms p95)
# - Error rate (should be 0%)
# - Database connection pool usage
```

## Cleanup

```sql
-- Clean up test data
DELETE FROM REVIEW_LIKE WHERE REVIEW_ID IN (1, 2, 999);
DELETE FROM REVIEW WHERE REVIEW_ID IN (1, 2, 999);
DELETE FROM CONTENT WHERE CONTENT_ID = 1;
DELETE FROM USERS WHERE USER_ID IN ('testuser1', 'testuser2');
COMMIT;
```

## Integration Test Checklist

- [ ] ReviewLike toggle creates/deletes database records
- [ ] Like count accurate in responses
- [ ] isLikedByCurrentUser correct for authenticated users
- [ ] isLikedByCurrentUser null for unauthenticated users
- [ ] POPULAR sort orders reviews by like count
- [ ] Cursor pagination works with both sort orders
- [ ] Own review like prevented (400)
- [ ] Non-existent review handled (404)
- [ ] Authentication required (401)
- [ ] Cascade delete works correctly
- [ ] Query performance acceptable (< 500ms)
- [ ] No N+1 query issues

## Next Steps

After integration tests pass:
1. Perform manual testing with Swagger UI
2. Review application logs for errors
3. Check database for data integrity
4. Proceed to final verification

