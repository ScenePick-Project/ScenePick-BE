# Execution Plan

## Detailed Analysis Summary

### Transformation Scope
- **Transformation Type**: Single component enhancement
- **Primary Changes**: Add ReviewLike feature to existing review package
- **Related Components**: Review package (controller, service, DAO, VO, DTOs)

### Change Impact Assessment
- **User-facing changes**: Yes - Users will see like buttons, like counts, and their like status on reviews
- **Structural changes**: No - No architectural changes, follows existing patterns
- **Data model changes**: No - Database schema already exists (REVIEW_LIKE table)
- **API changes**: Yes - New endpoint POST /api/v1/reviews/{reviewId}/like, modified Review response DTOs
- **NFR impact**: Minimal - Performance considerations for like count queries, security for authentication

### Component Relationships
```
ReviewLike Feature Components:
    |
    +-- ReviewLikeController (NEW)
    |   |
    |   +-- POST /api/v1/reviews/{reviewId}/like
    |
    +-- ReviewLikeCommandService (NEW)
    |   |
    |   +-- toggleReviewLike()
    |   +-- Uses: ReviewLikeDao, ReviewDao
    |
    +-- ReviewLikeDao (IMPLEMENT)
    |   |
    |   +-- Interface for ReviewLikeMapper.xml (EXISTING)
    |
    +-- ReviewLikeVo (IMPLEMENT)
    |   |
    |   +-- Maps to REVIEW_LIKE table (EXISTING)
    |
    +-- ReviewResponse.Review (MODIFY)
    |   |
    |   +-- Add: likeCount, isLikedByCurrentUser
    |
    +-- ReviewQueryService (MODIFY)
        |
        +-- Populate like data in review responses
```

**Dependencies**:
- ReviewLikeCommandService depends on ReviewLikeDao, ReviewDao
- ReviewQueryService depends on ReviewLikeDao (for counts and status)
- ReviewLikeController depends on ReviewLikeCommandService
- All components use existing infrastructure (ApiResponse, JWT, error handling)

### Risk Assessment
- **Risk Level**: Low
- **Rollback Complexity**: Easy - Feature is additive, no breaking changes to existing APIs
- **Testing Complexity**: Simple - Clear test scenarios (like, unlike, toggle, validation)
- **Rationale**: 
  - Database schema and SQL queries already complete and tested
  - Clear pattern to follow from existing Review implementation
  - No architectural changes or complex integrations
  - Isolated feature with minimal cross-component impact

---

## Workflow Visualization

```
                         User Request
                              |
                              v
        +-------------------------------------------------------+
        |     INCEPTION PHASE                                   |
        |     Planning & Application Design                     |
        +-------------------------------------------------------+
        | - Workspace Detection (COMPLETED)                     |
        | - Reverse Engineering (COMPLETED)                     |
        | - Requirements Analysis (COMPLETED)                   |
        | - User Stories (SKIPPED)                              |
        | - Workflow Planning (IN PROGRESS)                     |
        | - Application Design (SKIP)                           |
        | - Units Generation (SKIP)                             |
        +-------------------------------------------------------+
                              |
                              v
        +-------------------------------------------------------+
        |     CONSTRUCTION PHASE                                |
        |     Design, Implementation & Test                     |
        +-------------------------------------------------------+
        | - Per-Unit Loop (single unit):                        |
        |   - Functional Design (SKIP)                          |
        |   - NFR Requirements (SKIP)                           |
        |   - NFR Design (SKIP)                                 |
        |   - Infrastructure Design (SKIP)                      |
        |   - Code Generation (EXECUTE)                         |
        | - Build and Test (EXECUTE)                            |
        +-------------------------------------------------------+
                              |
                              v
        +-------------------------------------------------------+
        |     OPERATIONS PHASE                                  |
        |     Placeholder for Future                            |
        +-------------------------------------------------------+
        | - Operations (PLACEHOLDER)                            |
        +-------------------------------------------------------+
                              |
                              v
                          Complete
```

---

## Phases to Execute

### 🔵 INCEPTION PHASE
- [x] Workspace Detection (COMPLETED)
- [x] Reverse Engineering (COMPLETED)
- [x] Requirements Analysis (COMPLETED)
- [x] User Stories (SKIPPED)
- [x] Workflow Planning (IN PROGRESS)
- [ ] Application Design - SKIP
  - **Rationale**: No new components or complex service layer design needed. Requirements document already defines all components (ReviewLikeVo, ReviewLikeDao, ReviewLikeCommandService, ReviewLikeController, ReviewLikeResponse). Component methods are straightforward (toggleReviewLike, countReviewLikes, existsReviewLike). Clear pattern exists from Review implementation.
- [ ] Units Generation - SKIP
  - **Rationale**: Single cohesive unit of work. All ReviewLike components work together as one feature. No need to decompose into multiple units. Simple enough to implement as a single iteration.

### 🟢 CONSTRUCTION PHASE
- [ ] Functional Design - SKIP
  - **Rationale**: No complex business logic or data models. Toggle logic is simple (check existence, insert or delete). Database schema already defined. Business rules clearly documented in requirements (cannot like own review, authentication required).
- [ ] NFR Requirements - SKIP
  - **Rationale**: NFR requirements already documented in requirements.md (performance, security, data integrity, scalability, maintainability, reliability). Technology stack already determined (Spring Boot, MyBatis, Oracle). No new NFR considerations needed.
- [ ] NFR Design - SKIP
  - **Rationale**: No NFR-specific design patterns needed. Uses existing infrastructure (JWT authentication, database indexes, transaction management). Performance handled by database constraints and indexes. Security handled by existing Spring Security configuration.
- [ ] Infrastructure Design - SKIP
  - **Rationale**: No infrastructure changes needed. Uses existing database (Oracle), existing application server (Spring Boot), existing authentication (JWT). No new infrastructure services required.
- [ ] Code Generation - EXECUTE (ALWAYS)
  - **Rationale**: Implementation needed for ReviewLikeVo, ReviewLikeDao, ReviewLikeCommandService, ReviewLikeController, ReviewLikeResponse. Modifications needed for ReviewResponse.Review and ReviewQueryService. Clear implementation path following existing patterns.
- [ ] Build and Test - EXECUTE (ALWAYS)
  - **Rationale**: Build verification, unit tests, integration tests, API tests needed. Ensure like operations work correctly, validation works, error handling works, integration with existing Review APIs works.

### 🟡 OPERATIONS PHASE
- [ ] Operations - PLACEHOLDER
  - **Rationale**: Future deployment and monitoring workflows

---

## Execution Summary

### Stages to Execute: 2
1. **Code Generation** - Implement ReviewLike feature components
2. **Build and Test** - Verify implementation and test all scenarios

### Stages to Skip: 8
1. **User Stories** - Requirements already user-centered and clear
2. **Application Design** - Components already defined in requirements
3. **Units Generation** - Single unit of work, no decomposition needed
4. **Functional Design** - Simple logic, no complex design needed
5. **NFR Requirements** - Already documented in requirements
6. **NFR Design** - Uses existing infrastructure patterns
7. **Infrastructure Design** - No infrastructure changes
8. **Operations** - Placeholder for future

---

## Implementation Approach

### Single Unit: ReviewLike Feature

**Components to Implement**:
1. ReviewLikeVo.java - Database entity (5 fields)
2. ReviewLikeDao.java - Data access interface (6 methods including popularity sort)
3. ReviewLikeCommandService.java - Business logic (1 method: toggleReviewLike)
4. ReviewLikeController.java - REST endpoint (1 endpoint: POST /like)
5. ReviewLikeResponse.java - Response DTO (3 fields)

**Components to Modify**:
1. ReviewRequest.Slice - Add sortBy and cursorLikeCount fields
2. ReviewResponse.Review - Add likeCount and isLikedByCurrentUser fields
3. ReviewResponse.Cursor - Add likeCount field for POPULAR sort
4. ReviewDao.java - Add selectReviewCursorByPopularity method
5. ReviewMapper.xml - Add SQL query for popularity sorting
6. ReviewQueryService - Populate like data and support both sort orders

**Implementation Pattern**:
- Follow existing Review package structure exactly
- Use Lombok for boilerplate reduction
- Use Spring annotations (@RestController, @Service, @Transactional)
- Use existing ApiResponse wrapper for responses
- Use existing JWT authentication (@AuthenticationPrincipal)
- Use existing error handling (ExceptionAdvice)
- Add Swagger annotations (@Operation, @Schema)

---

## Estimated Timeline

- **Total Phases**: 2 (Code Generation + Build and Test)
- **Estimated Duration**: 1.5-2.5 hours
  - Code Generation: 60-90 minutes (11 files to create/modify, including popularity sort logic)
  - Build and Test: 30-45 minutes (compile, test, verify sorting functionality)

---

## Success Criteria

### Primary Goal
Implement ReviewLike feature allowing users to like/unlike reviews with toggle functionality

### Key Deliverables
1. ✅ ReviewLikeVo.java implemented
2. ✅ ReviewLikeDao.java implemented (including popularity sort method)
3. ✅ ReviewLikeCommandService.java implemented
4. ✅ ReviewLikeController.java implemented
5. ✅ ReviewLikeResponse.java implemented
6. ✅ ReviewRequest.Slice modified with sortBy and cursorLikeCount
7. ✅ ReviewResponse.Review modified with like fields
8. ✅ ReviewResponse.Cursor modified with likeCount
9. ✅ ReviewDao.java modified with popularity sort method
10. ✅ ReviewMapper.xml modified with popularity sort query
11. ✅ ReviewQueryService modified to populate like data and support sorting
12. ✅ All code compiles without errors
13. ✅ Checkstyle validation passes (Naver conventions)
14. ✅ Swagger documentation complete

### Quality Gates
- Code follows existing Review package patterns
- All business rules implemented (cannot like own review)
- Authentication and authorization working
- Error handling consistent with existing APIs
- Database constraints prevent duplicate likes
- Like counts accurate and performant
- API responses match specifications

### Functional Verification
- ✅ User can like a review (POST /api/v1/reviews/{reviewId}/like)
- ✅ User can unlike a review (POST same endpoint toggles)
- ✅ Like count displayed in review responses
- ✅ User like status shown when authenticated
- ✅ Users cannot like their own reviews (validation error)
- ✅ Unauthenticated users cannot like (401 error)
- ✅ Non-existent review returns 404 error
- ✅ Duplicate like toggles correctly (idempotent)
- ✅ Review list defaults to LATEST sort (backward compatible)
- ✅ Review list with sortBy=POPULAR shows most-liked reviews first
- ✅ Cursor pagination works with LATEST sort
- ✅ Cursor pagination works with POPULAR sort
- ✅ Reviews with same like count sorted by creation date (newest first)

