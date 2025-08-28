package com.simprints.infra.eventsync.event.commcare.cache

import com.simprints.infra.logging.Simber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommCareSyncCache @Inject constructor(
    private val commCareSyncDao: CommCareSyncDao,
) {

    suspend fun addSyncedCase(case: SyncedCaseEntity) = commCareSyncDao.insert(case).also {
        Simber.d("Added/Updated case: ${case.caseId} -> ${case.simprintsId} with timestamp ${case.lastSyncedTimestamp} in CommCareSyncCache (DB)")
    }

    suspend fun getSimprintsId(caseId: String): String? = commCareSyncDao.getByCaseId(caseId).also { entity ->
        Simber.d("Retrieved simprintsId for case: $caseId -> ${entity?.simprintsId} from CommCareSyncCache (DB)")
    }?.simprintsId

    suspend fun removeSyncedCase(caseId: String) = commCareSyncDao.deleteByCaseId(caseId).also {
        Simber.d("Removed case: $caseId from CommCareSyncCache (DB)")
    }

    suspend fun getAllSyncedCases(): List<SyncedCaseEntity> = commCareSyncDao.getAll().also { allEntries ->
        Simber.d("Retrieved all ${allEntries.size} case entities from CommCareSyncCache (DB)")
    }

    suspend fun clearAllSyncedCases() = commCareSyncDao.clearAll().also {
        Simber.d("Cleared all cases from CommCareSyncCache (DB)")
    }
}
