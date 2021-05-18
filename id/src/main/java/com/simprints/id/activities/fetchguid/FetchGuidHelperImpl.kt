package com.simprints.id.activities.fetchguid

import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.eventsystem.SubjectFetchResult
import com.simprints.eventsystem.SubjectFetchResult.SubjectSource.*
import com.simprints.eventsystem.subject.SubjectRepository
import com.simprints.eventsystem.subject.local.SubjectQuery
import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.toList
import timber.log.Timber

class FetchGuidHelperImpl(private val downSyncHelper: EventDownSyncHelper,
                          val subjectRepository: SubjectRepository,
                          val preferencesManager: PreferencesManager,
                          val crashReportManager: CrashReportManager) : FetchGuidHelper {

    override suspend fun loadFromRemoteIfNeeded(coroutineScope: CoroutineScope, projectId: String, subjectId: String): SubjectFetchResult {
        return try {
            val subjectResultFromDB = fetchFromLocalDb(projectId, subjectId)
            Timber.d("[FETCH_GUID] Fetching $subjectId")
            return if (subjectResultFromDB != null) {
                Timber.d("[FETCH_GUID] Guid found in Local")

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

                Timber.d("[FETCH_GUID] Network request done")

                val subjectResult = fetchFromLocalDb(projectId, subjectId)

                if (subjectResult != null) {
                    Timber.d("[FETCH_GUID] Guid found in Remote")

                    SubjectFetchResult(subjectResult.subject, REMOTE)
                } else {
                    Timber.d("[FETCH_GUID] Guid found not")

                    SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE)
                }
            }
        } catch (t: Throwable) {
            Timber.e(t)
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
