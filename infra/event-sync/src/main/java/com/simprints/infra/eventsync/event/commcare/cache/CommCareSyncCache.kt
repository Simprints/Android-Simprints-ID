package com.simprints.infra.eventsync.event.commcare.cache

import com.simprints.core.DispatcherIO
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommCareSyncCache @Inject constructor(
    private val commCareSyncDao: CommCareSyncDao,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher,
) {

    suspend fun addSyncedCase(case: SyncedCaseEntity) {
        withContext(dispatcherIO) {
            commCareSyncDao.insert(case)
            Simber.d("Added/Updated case: ${case.caseId} -> ${case.simprintsId} with timestamp ${case.lastSyncedTimestamp} in CommCareSyncCache (DB)")
        }
    }

    suspend fun getSimprintsId(caseId: String): String? {
        return withContext(dispatcherIO) {
            val entity = commCareSyncDao.getByCaseId(caseId)
            Simber.d("Retrieved simprintsId for case: $caseId -> ${entity?.simprintsId} from CommCareSyncCache (DB)")
            entity?.simprintsId
        }
    }

    suspend fun removeSyncedCase(caseId: String) {
        withContext(dispatcherIO) {
            commCareSyncDao.deleteByCaseId(caseId)
            Simber.d("Removed case: $caseId from CommCareSyncCache (DB)")
        }
    }

    suspend fun getAllSyncedCases(): List<SyncedCaseEntity> {
        return withContext(dispatcherIO) {
            val allEntries = commCareSyncDao.getAll()
            Simber.d("Retrieved all ${allEntries.size} case entities from CommCareSyncCache (DB)")
            allEntries
        }
    }

    suspend fun clearAllSyncedCases() {
        withContext(dispatcherIO) {
            commCareSyncDao.clearAll()
            Simber.d("Cleared all cases from CommCareSyncCache (DB)")
        }
    }
}
