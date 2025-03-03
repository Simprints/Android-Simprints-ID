package com.simprints.feature.fetchsubject.screen.usecase

import com.simprints.feature.fetchsubject.screen.FetchSubjectState
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
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
        Simber.d("Fetching $subjectId", tag = TAG)
        try {
            val localSubject = loadFromDatabase(projectId, subjectId)
            if (localSubject != null) {
                Simber.d("Guid found in Local", tag = TAG)
                return FetchSubjectState.FoundLocal
            }

            eventSyncManager.downSyncSubject(projectId, subjectId)
            Simber.d("Network request done", tag = TAG)

            val remoteSubject = loadFromDatabase(projectId, subjectId)
            if (remoteSubject != null) {
                Simber.d("Guid found in Remote", tag = TAG)
                return FetchSubjectState.FoundRemote
            }

            Simber.d("Guid found not", tag = TAG)

            return notFoundState()
        } catch (t: Throwable) {
            Simber.e("Error fetching", t, tag = TAG)
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
