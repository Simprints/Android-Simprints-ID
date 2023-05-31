package com.simprints.infra.authlogic.securitystate.worker

import com.simprints.infra.authlogic.authenticator.SignerManager
import com.simprints.infra.authlogic.securitystate.models.SecurityState
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.events.EventRepository
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class SecurityStateProcessor @Inject constructor(
    private val imageRepository: ImageRepository,
    private val enrolmentRecordManager: EnrolmentRecordManager,
    private val eventRepository: EventRepository,
    private val signerManager: SignerManager
) {

    suspend fun processSecurityState(securityState: SecurityState) {
        if (securityState.status.isCompromisedOrProjectEnded()) {
            deleteLocalData()
            signOut()
        }

        if (securityState.mustUpSyncEnrolmentRecords != null) {
            Simber.i("subject ids ${securityState.mustUpSyncEnrolmentRecords.subjectIds.size}")
            enrolmentRecordManager.upload(
                securityState.mustUpSyncEnrolmentRecords.id,
                securityState.mustUpSyncEnrolmentRecords.subjectIds
            )
        }
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
