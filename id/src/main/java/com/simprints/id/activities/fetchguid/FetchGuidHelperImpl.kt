package com.simprints.id.activities.fetchguid

import com.simprints.id.data.db.SubjectFetchResult
import com.simprints.id.data.db.SubjectFetchResult.SubjectSource.*
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncScope.SubjectScope
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource.Query
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modality.toMode
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.toList

class FetchGuidHelperImpl(val downSyncHelper: EventDownSyncHelper,
                          val subjectRepository: SubjectRepository,
                          val preferencesManager: PreferencesManager) : FetchGuidHelper {

    override suspend fun loadFromRemoteIfNeeded(coroutineScope: CoroutineScope, projectId: String, subjectId: String): SubjectFetchResult {
        val subjectResultFromDB = fetchFromLocalDb(projectId, subjectId)

        return if (subjectResultFromDB != null) {
            subjectResultFromDB
        } else {
            val downSyncScopeOps = SubjectScope(projectId, subjectId, preferencesManager.modalities.map { it.toMode() }).operations
            val subjectsChannel = downSyncHelper.downSync(coroutineScope, downSyncScopeOps.first())
            while (!subjectsChannel.isClosedForReceive) {
                //
            }
            val subjectResult = fetchFromLocalDb(projectId, subjectId)

            if (subjectResult != null) {
                SubjectFetchResult(subjectResult.subject, REMOTE)
            } else {
                SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE)
            }
        }
    }

    private suspend fun fetchFromLocalDb(projectId: String, subjectId: String): SubjectFetchResult? {
        val subject = subjectRepository.load(Query(projectId = projectId, subjectId = subjectId)).toList().firstOrNull()
        return subject?.let {
            SubjectFetchResult(subject, LOCAL)
        }
    }
}
