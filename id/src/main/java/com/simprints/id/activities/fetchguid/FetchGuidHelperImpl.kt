package com.simprints.id.activities.fetchguid

import com.simprints.id.data.db.SubjectFetchResult
import com.simprints.id.data.db.SubjectFetchResult.SubjectSource.*
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.flow.toList
import javax.inject.Inject

class FetchGuidHelperImpl @Inject constructor(
    private val eventSyncManager: EventSyncManager,
    private val enrolmentRecordManager: EnrolmentRecordManager,
    private val configManager: ConfigManager,
) : FetchGuidHelper {

    override suspend fun loadFromRemoteIfNeeded(projectId: String, subjectId: String): SubjectFetchResult = try {
        val subjectResultFromDB = fetchFromLocalDb(projectId, subjectId)
        Simber.d("[FETCH_GUID] Fetching $subjectId")

        if (subjectResultFromDB != null) {
            Simber.d("[FETCH_GUID] Guid found in Local")
            subjectResultFromDB
        } else {
            eventSyncManager.downSyncSubject(
                projectId = projectId,
                subjectId = subjectId,
            )

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
