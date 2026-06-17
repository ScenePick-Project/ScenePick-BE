# Unit Test Execution

## Overview

**Testing Approach**: Standard code generation (no TDD artifacts found)
- Unit tests need to be created and executed
- Focus on testing individual components in isolation

## Test Strategy

### Components to Test

1. **ReviewLikeCommandService**
   - toggleReviewLike() method
   - Business logic validation
   - Transaction handling

2. **ReviewQueryService** (Modified)
   - enrichReviewWithLikeData() method
   - getReview() with like data
   - getReviewList() with sorting

3. **ReviewLikeController**
   - POST /api/v1/reviews/{reviewId}/like endpoint
   - Authentication handling
   - Response format

## Recommended Unit Tests

### 1. ReviewLikeCommandService Tests

Create `src/test/java/com/project/scenepickbe/review/service/ReviewLikeCommandServiceTest.java`:

```java
@ExtendWith(MockitoExtension.class)
class ReviewLikeCommandServiceTest {
    
    @Mock
    private ReviewLikeDao reviewLikeDao;
    
    @Mock
    private ReviewDao reviewDao;
    
    @InjectMocks
    private ReviewLikeCommandService reviewLikeCommandService;
    
    @Test
    void toggleReviewLike_WhenNotLiked_ShouldAddLike() {
        // Test: User likes a review they haven't liked before
        // Expected: Like is added, isLiked=true, count increases
    }
    
    @Test
    void toggleReviewLike_WhenAlreadyLiked_ShouldRemoveLike() {
        // Test: User unlikes a review they already liked
        // Expected: Like is removed, isLiked=false, count decreases
    }
    
    @Test
    void toggleReviewLike_WhenOwnReview_ShouldThrowException() {
        // Test: User tries to like their own review
        // Expected: GeneralException with BAD_REQUEST status
    }
    
    @Test
    void toggleReviewLike_WhenReviewNotFound_ShouldThrowException() {
        // Test: User tries to like non-existent review
        // Expected: GeneralException with NOT_FOUND status
    }
}
```

### 2. ReviewQueryService Tests

Create `src/test/java/com/project/scenepickbe/review/service/ReviewQueryServiceTest.java`:

```java
@ExtendWith(MockitoExtension.class)
class ReviewQueryServiceTest {
    
    @Mock
    private ReviewDao reviewDao;
    
    @Mock
    private ReviewLikeDao reviewLikeDao;
    
    @Mock
    private ReviewDtoMapper reviewDtoMapper;
    
    @InjectMocks
    private ReviewQueryService reviewQueryService;
    
    @Test
    void getReview_ShouldIncludeLikeCount() {
        // Test: Review includes like count
        // Expected: likeCount field populated
    }
    
    @Test
    void getReview_WhenAuthenticated_ShouldIncludeLikeStatus() {
        // Test: Authenticated user gets like status
        // Expected: isLikedByCurrentUser populated
    }
    
    @Test
    void getReview_WhenNotAuthenticated_ShouldHaveNullLikeStatus() {
        // Test: Unauthenticated user gets null like status
        // Expected: isLikedByCurrentUser is null
    }
    
    @Test
    void getReviewList_WithLatestSort_ShouldSortByCreatedAt() {
        // Test: LATEST sort uses creation date
        // Expected: Reviews sorted by CREATED_AT DESC
    }
    
    @Test
    void getReviewList_WithPopularSort_ShouldSortByLikeCount() {
        // Test: POPULAR sort uses like count
        // Expected: Reviews sorted by like_count DESC
    }
}
```

### 3. ReviewLikeController Tests

Create `src/test/java/com/project/scenepickbe/review/controller/ReviewLikeControllerTest.java`:

```java
@WebMvcTest(ReviewLikeController.class)
class ReviewLikeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ReviewLikeCommandService reviewLikeCommandService;
    
    @Test
    @WithMockUser(username = "user123")
    void toggleReviewLike_WhenAuthenticated_ShouldReturnSuccess() throws Exception {
        // Test: Authenticated user can toggle like
        // Expected: 200 OK with ReviewLikeResponse
    }
    
    @Test
    void toggleReviewLike_WhenNotAuthenticated_ShouldReturn401() throws Exception {
        // Test: Unauthenticated user cannot toggle like
        // Expected: 401 Unauthorized
    }
}
```

## Execute Unit Tests

### 1. Run All Unit Tests
```bash
# Execute all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests ReviewLikeCommandServiceTest

# Run with detailed output
./gradlew test --info
```

### 2. Review Test Results
- **Test Report Location**: `build/reports/tests/test/index.html`
- **Expected**: All tests pass (or create tests as recommended above)
- **Test Coverage**: Check `build/reports/jacoco/test/html/index.html` (if JaCoCo configured)

### 3. Analyze Test Output
```bash
# View test summary
cat build/test-results/test/TEST-*.xml

# Check for failures
grep -r "FAILED" build/test-results/test/
```

## Manual Testing Alternative

If unit tests are not yet created, perform manual testing:

### 1. Start Application
```bash
# Run Spring Boot application
./gradlew bootRun

# Application should start on http://localhost:8080
```

### 2. Access Swagger UI
```
Open browser: http://localhost:8080/swagger-ui.html
```

### 3. Test ReviewLike Endpoints

**Test POST /api/v1/reviews/{reviewId}/like**:
1. Authenticate (get JWT token)
2. Find a review ID from GET /api/v1/contents/{contentId}/reviews
3. POST to /api/v1/reviews/{reviewId}/like
4. Verify response includes isLiked and likeCount
5. POST again to same endpoint
6. Verify isLiked toggles and count changes

**Test GET /api/v1/contents/{contentId}/reviews with sorting**:
1. GET /api/v1/contents/{contentId}/reviews?sortBy=LATEST
2. Verify reviews sorted by creation date
3. GET /api/v1/contents/{contentId}/reviews?sortBy=POPULAR
4. Verify reviews sorted by like count
5. Verify each review includes likeCount and isLikedByCurrentUser

## Test Scenarios Checklist

### ReviewLike Toggle
- [ ] User can like a review (not previously liked)
- [ ] User can unlike a review (already liked)
- [ ] User cannot like their own review (400 error)
- [ ] Unauthenticated user cannot like (401 error)
- [ ] Non-existent review returns 404 error
- [ ] Like count updates correctly after toggle

### Review List with Like Data
- [ ] Review list includes like count for each review
- [ ] Authenticated user sees isLikedByCurrentUser
- [ ] Unauthenticated user sees null isLikedByCurrentUser
- [ ] Like count is accurate

### Popularity Sorting
- [ ] sortBy=LATEST sorts by creation date (default)
- [ ] sortBy=POPULAR sorts by like count
- [ ] Reviews with same like count sorted by creation date
- [ ] Cursor pagination works with LATEST sort
- [ ] Cursor pagination works with POPULAR sort

## Fix Failing Tests

If tests fail:
1. Review test output in `build/reports/tests/test/index.html`
2. Identify failing test cases and error messages
3. Common issues:
   - Mock setup incorrect: Verify mock behavior matches actual usage
   - Null pointer exceptions: Check for null safety in code
   - Assertion failures: Verify expected vs actual values
4. Fix code issues in source files
5. Rerun tests: `./gradlew test`
6. Repeat until all tests pass

## Test Coverage Goals

- **Service Layer**: 80%+ coverage
- **Controller Layer**: 70%+ coverage
- **DAO Layer**: Covered by integration tests

## Next Steps

After unit tests pass:
1. Proceed to Integration Test Execution
2. Test database interactions with actual Oracle DB
3. Verify MyBatis mapper queries work correctly

