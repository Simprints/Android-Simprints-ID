package com.simprints.id.activities.fetchguid

import com.simprints.core.analytics.CrashReportManager
import com.simprints.core.domain.modality.toMode
import com.simprints.id.data.db.SubjectFetchResult
import com.simprints.id.data.db.SubjectFetchResult.SubjectSource.*
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.local.SubjectQuery
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.logging.Simber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.toList

class FetchGuidHelperImpl(private val downSyncHelper: EventDownSyncHelper,
                          val subjectRepository: SubjectRepository,
                          val preferencesManager: IdPreferencesManager,
                          val crashReportManager: CrashReportManager
) : FetchGuidHelper {

    override suspend fun loadFromRemoteIfNeeded(coroutineScope: CoroutineScope, projectId: String, subjectId: String): SubjectFetchResult {
        return try {
            val subjectResultFromDB = fetchFromLocalDb(projectId, subjectId)
            Simber.d("[FETCH_GUID] Fetching $subjectId")
            return if (subjectResultFromDB != null) {
                Simber.d("[FETCH_GUID] Guid found in Local")

                subjectResultFromDB
            } else {
                val op = com.simprints.eventsystem.events_sync.down.domain.EventDownSyncOperation(
                    com.simprints.eventsystem.events_sync.down.domain.RemoteEventQuery(
                        projectId,
                        subjectId = subjectId,
                        modes = preferencesManager.modalities.map { it.toMode() },
                        types = com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope.subjectEvents
                    )
                )

                downSyncHelper.downSync(coroutineScope, op).consumeAsFlow().toList()

                Simber.d("[FETCH_GUID] Network request done")

                val subjectResult = fetchFromLocalDb(projectId, subjectId)

                if (subjectResult != null) {
                    Simber.d("[FETCH_GUID] Guid found in Remote")

                    SubjectFetchResult(subjectResult.subject, REMOTE)
                } else {
                    Simber.d("[FETCH_GUID] Guid found not")

                    SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE)
                }
            }
        } catch (t: Throwable) {
            Simber.e(t)
            crashReportManager.logException(t)
            SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE)
        }
    }

    private suspend fun fetchFromLocalDb(projectId: String, subjectId: String): SubjectFetchResult? {
        val subject = subjectRepository.load(SubjectQuery(projectId = projectId, subjectId = subjectId)).toList().firstOrNull()
        return subject?.let {
            SubjectFetchResult(subject, LOCAL)
        }
    }
}
