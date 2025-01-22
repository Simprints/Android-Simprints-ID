package com.simprints.feature.fetchsubject.screen.usecase

import com.simprints.feature.fetchsubject.screen.FetchSubjectState
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.ConnectivityTracker
import javax.inject.Inject

internal class FetchSubjectUseCase @Inject constructor(
    private val connectivityTracker: ConnectivityTracker,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val eventSyncManager: EventSyncManager,
) {
    suspend operator fun invoke(
        projectId: String,
        subjectId: String,
    ): FetchSubjectState {
        Simber.tag(TAG).d("Fetching $subjectId")
        try {
            val localSubject = loadFromDatabase(projectId, subjectId)
            if (localSubject != null) {
                Simber.tag(TAG).d("Guid found in Local")
                return FetchSubjectState.FoundLocal
            }

            eventSyncManager.downSyncSubject(projectId, subjectId)
            Simber.tag(TAG).d("Network request done")

            val remoteSubject = loadFromDatabase(projectId, subjectId)
            if (remoteSubject != null) {
                Simber.tag(TAG).d("Guid found in Remote")
                return FetchSubjectState.FoundRemote
            }

            Simber.tag(TAG).d("Guid found not")

            return notFoundState()
        } catch (t: Throwable) {
            Simber.tag(TAG).e("Error fetching", t)
            return notFoundState()
        }
    }

    private suspend fun loadFromDatabase(
        projectId: String,
        subjectId: String,
    ) = enrolmentRecordRepository.load(SubjectQuery(projectId, subjectId)).firstOrNull()

    private fun notFoundState() = if (connectivityTracker.isConnected()) {
        FetchSubjectState.NotFound
    } else {
        FetchSubjectState.ConnectionError
    }

    companion object {
        private const val TAG = "FETCH_GUID"
    }
}
