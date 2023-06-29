package com.simprints.infra.authlogic.worker

import com.simprints.infra.authlogic.authenticator.SignerManager
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.events.EventRepository
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.logging.Simber
import com.simprints.infra.projectsecuritystore.securitystate.models.SecurityState
import kotlinx.coroutines.flow.first
import javax.inject.Inject

internal class SecurityStateProcessor @Inject constructor(
    private val imageRepository: ImageRepository,
    private val enrolmentRecordManager: EnrolmentRecordManager,
    private val eventRepository: EventRepository,
    private val eventSyncManager: EventSyncManager,
    private val signerManager: SignerManager
) {

    suspend fun processSecurityState(securityState: SecurityState) {
        if (shouldSignOut(securityState)) {
            deleteLocalData()
            signOut()
        } else {
            securityState.mustUpSyncEnrolmentRecords?.let { upSyncEnrolmentRecords ->
                Simber.i("subject ids ${upSyncEnrolmentRecords.subjectIds.size}")
                enrolmentRecordManager.upload(
                    upSyncEnrolmentRecords.id,
                    upSyncEnrolmentRecords.subjectIds
                )
            }
        }
    }

    private suspend fun shouldSignOut(securityState: SecurityState): Boolean {
        val isProjectEnded = securityState.status.isCompromisedOrProjectEnded()
        val isProjectEnding = securityState.status == SecurityState.Status.PROJECT_ENDING
        val hasNoEventsToUpload = eventSyncManager.countEventsToUpload(
            projectId = signerManager.signedInProjectId,
            type = null
        ).first() == 0

        return isProjectEnded || (isProjectEnding && hasNoEventsToUpload)
    }

    private suspend fun deleteLocalData() {
        imageRepository.deleteStoredImages()
        eventRepository.deleteAll()
        enrolmentRecordManager.deleteAll()
    }

    private suspend fun signOut() {
        signerManager.signOut()
    }
}
