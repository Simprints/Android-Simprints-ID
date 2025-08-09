# CommCare Case Sync Tracking

This module provides functionality to track CommCare case synchronization state, maintaining a mapping between CommCare case IDs, Simprints subject IDs, and last modified timestamps.

## Components

### Database Layer
- **DbCommCareCase**: Room entity storing case tracking information
- **CommCareCaseDao**: Data access object for database operations
- **Migration 1->2**: Database migration to add the CommCare case table

### Repository Layer
- **CommCareCaseRepository**: Interface for case tracking operations
- **CommCareCaseRepositoryImpl**: Implementation using Room database

### Sync Layer
- **CommCareEventSyncTask**: Syncs case data from CommCare content provider

## Usage

### Syncing Cases from CommCare
```kotlin
@Inject
lateinit var commCareEventSyncTask: CommCareEventSyncTask

// Sync all cases from CommCare app
val syncedCases = commCareEventSyncTask.syncCommCareCases("org.commcare.dalvik")
```

### Repository Operations
```kotlin
@Inject
lateinit var commCareCaseRepository: CommCareCaseRepository

// Save case tracking record
val case = CommCareCase(
    caseId = "case123",
    subjectId = "subject456", 
    lastModified = System.currentTimeMillis()
)
commCareCaseRepository.saveCase(case)

// Get case by ID
val case = commCareCaseRepository.getCase("case123")

// Delete case when subject is removed
commCareCaseRepository.deleteCaseBySubjectId("subject456")
```

## Integration Points

### Automatic Deletion on Subject Removal
When a subject is deleted through `RoomEnrolmentRecordLocalDataSource.deleteSubject()`, the corresponding CommCare case tracking record is automatically removed.

### Content Provider Schema
The implementation expects CommCare's content provider to expose:
- Case metadata: `content://{package}.case/casedb/case`
  - `case_id`: Case identifier
  - `last_modified`: Timestamp of last modification
- Case data: `content://{package}.case/casedb/data/{caseId}`
  - `datum_id`: Field name
  - `value`: Field value
  - Looking for `simprintsId` field as the subject ID

## Database Schema
```sql
CREATE TABLE DbCommCareCase (
    caseId TEXT NOT NULL PRIMARY KEY,
    subjectId TEXT NOT NULL,
    lastModified INTEGER NOT NULL
);

CREATE INDEX index_DbCommCareCase_caseId ON DbCommCareCase (caseId);
CREATE INDEX index_DbCommCareCase_subjectId ON DbCommCareCase (subjectId);
```