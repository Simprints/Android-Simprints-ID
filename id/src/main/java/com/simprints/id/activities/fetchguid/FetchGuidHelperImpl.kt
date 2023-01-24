package com.simprints.id.activities.fetchguid

import com.simprints.core.DispatcherIO
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncOperation
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope
import com.simprints.eventsystem.events_sync.down.domain.RemoteEventQuery
import com.simprints.id.data.db.SubjectFetchResult
import com.simprints.id.data.db.SubjectFetchResult.SubjectSource.*
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.tools.extensions.toMode
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FetchGuidHelperImpl @Inject constructor(
    private val downSyncHelper: EventDownSyncHelper,
    private val enrolmentRecordManager: EnrolmentRecordManager,
    private val configManager: ConfigManager,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) : FetchGuidHelper {

    override suspend fun loadFromRemoteIfNeeded(
        projectId: String,
        subjectId: String
    ): SubjectFetchResult = withContext(dispatcher) {
        return@withContext try {
            val subjectResultFromDB = fetchFromLocalDb(projectId, subjectId)
            Simber.d("[FETCH_GUID] Fetching $subjectId")
            return@withContext if (subjectResultFromDB != null) {
                Simber.d("[FETCH_GUID] Guid found in Local")

                subjectResultFromDB
            } else {
                val op = EventDownSyncOperation(
                    RemoteEventQuery(
                        projectId,
                        subjectId = subjectId,
                        modes = configManager.getProjectConfiguration().general.modalities.map { it.toMode() },
                        types = EventDownSyncScope.subjectEvents
                    )
                )

                downSyncHelper.downSync(this, op).consumeAsFlow().toList()

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
            SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE)
        }
    }

    private suspend fun fetchFromLocalDb(
        projectId: String,
        subjectId: String
    ): SubjectFetchResult? {
        val subject =
            enrolmentRecordManager.load(SubjectQuery(projectId = projectId, subjectId = subjectId))
                .toList().firstOrNull()
        return subject?.let {
            SubjectFetchResult(subject, LOCAL)
        }
    }
}
