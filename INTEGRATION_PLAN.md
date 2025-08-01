# CommCare Case Sync Integration Plan

## Current Status
- ✅ Database layer implemented (DbCommCareCase, CommCareCaseDao, CommCareCaseRepository)
- ✅ Removed duplicate CommCareEventSyncTask from infra/enrolment-records
- ✅ Repository pattern and domain models ready
- ⏳ Need to integrate with existing CommCareEventSyncTask in MS-1044-co-down-sync branch

## Integration Strategy

### 1. Database Infrastructure (Completed)
The following components are ready and tested:
- `DbCommCareCase`: Room entity for case tracking
- `CommCareCaseDao`: Data access operations
- `CommCareCaseRepository`: Repository interface and implementation
- Database migration from v1 to v2

### 2. Required Integration Points

#### A. Existing CommCareEventSyncTask Enhancement
Location: `infra/event-sync/src/main/java/com/simprints/infra/eventsync/sync/down/tasks/CommCareEventSyncTask.kt`

Required changes:
1. Add `CommCareCaseRepository` dependency injection
2. Add case tracking in `fetchEvents()` method
3. Extract caseId, subjectId, last_modified from CommCare content provider
4. Save case tracking records during sync
5. Clean up case records when subjects are deleted

#### B. Content Provider Data Extraction
Add methods to extract:
- `case_id` from case metadata
- `last_modified` timestamp from case metadata  
- `simprintsId` field from case data to get subjectId

#### C. Cleanup Integration
Modify subject deletion cleanup to also remove case tracking records.

### 3. Technical Approach

#### Option 1: Cherry-pick Integration (Recommended)
1. Create new branch from MS-1044-co-down-sync
2. Cherry-pick database components from current implementation
3. Manually integrate case tracking into existing CommCareEventSyncTask
4. Test and validate integration

#### Option 2: Manual Code Integration
1. Extract existing CommCareEventSyncTask from MS-1044 branch
2. Add case tracking functionality line by line
3. Apply to current branch
4. Test integration

### 4. Key Integration Code Snippets

#### CommCareEventSyncTask Constructor Update:
```kotlin
internal class CommCareEventSyncTask @Inject constructor(
    // ... existing parameters
    private val commCareCaseRepository: CommCareCaseRepository,
    @ApplicationContext private val context: Context,
)
```

#### Case Tracking in fetchEvents():
```kotlin
override suspend fun fetchEvents(...): EventFetchResult {
    Simber.i("CommCareEventSyncTask started", tag = COMMCARE_SYNC)
    
    // Add case tracking
    syncCommCareCases()
    
    val result = commCareEventDataSource.getEvents()
    return EventFetchResult(...)
}
```

#### Case Extraction Logic:
```kotlin
private suspend fun syncCommCareCases() {
    // Query CommCare content provider for case metadata
    // Extract case_id, last_modified
    // For each case, get subjectId from case data
    // Save CommCareCase(caseId, subjectId, lastModified)
}
```

## Next Steps
1. Resolve merge conflict approach with reviewer
2. Apply integration strategy 
3. Test case tracking functionality
4. Validate cleanup on subject deletion
5. Update documentation and examples

The core functionality is implemented and ready for integration once the branch strategy is confirmed.