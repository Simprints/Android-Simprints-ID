# CommCare Case Sync Cache

This module provides functionality to track CommCare case sync information using SecureSharedPreferences.

## Components

### CommCareCaseSyncInfo
Data class representing sync information for a CommCare case:
- `caseId`: The CommCare case identifier
- `subjectId`: The Simprints subject ID (simprintsId field in CommCare)
- `lastModified`: Timestamp of when this case was last modified/synced

### CommCareCaseSyncCache
Singleton service that provides secure storage for case sync information using encrypted SharedPreferences:
- `saveCaseSyncInfo(caseId, subjectId)`: Save or update case sync information
- `updateCaseLastModified(caseId)`: Update the last_modified timestamp for an existing case
- `deleteCaseSyncInfo(caseId)`: Delete sync information for a case
- `getCaseSyncInfo(caseId)`: Get sync information for a specific case
- `getCaseSyncInfoBySubjectId(subjectId)`: Get case sync information by subject ID
- `getAllCaseSyncInfo()`: Get all stored case sync information
- `clearAllCaseSyncInfo()`: Clear all stored case sync information

### CommCareSyncService
Service that coordinates case tracking with subject actions:
- `processSubjectActionsWithCaseTracking()`: Process subject actions and update case cache
- `syncDeletedCases()`: Remove cases from cache that are no longer present in CommCare

### Integration
The cache is automatically integrated into `EnrolmentRecordRepositoryImpl`:
- When subjects are created/updated/deleted via `performActions()`, case information is tracked
- `syncCommCareCaseCache(currentCommCareCaseIds)` can be called to clean up deleted cases

## Usage

The system automatically tracks case information when subjects are processed through the standard enrolment record operations. For manual operations:

```kotlin
// Inject the repository
@Inject
lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

// After syncing from CommCare, clean up deleted cases
val currentCaseIds = setOf("case1", "case2", "case3") // From CommCare
enrolmentRecordRepository.syncCommCareCaseCache(currentCaseIds)
```

For direct cache access:
```kotlin
// Inject the cache
@Inject
lateinit var commCareCaseSyncCache: CommCareCaseSyncCache

// Get case information
val caseInfo = commCareCaseSyncCache.getCaseSyncInfo("case123")
val allCases = commCareCaseSyncCache.getAllCaseSyncInfo()
```