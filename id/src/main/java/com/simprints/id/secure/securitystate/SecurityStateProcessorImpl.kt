package com.simprints.id.secure.securitystate

import com.simprints.id.secure.SignerManager
import com.simprints.id.secure.models.SecurityState
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.logging.Simber

class SecurityStateProcessorImpl(
    private val imageRepository: ImageRepository,
    private val enrolmentRecordManager: EnrolmentRecordManager,
    private val signerManager: SignerManager
) : SecurityStateProcessor {

    override suspend fun processSecurityState(securityState: SecurityState) {
        if (securityState.status.isCompromisedOrProjectEnded())
            deleteLocalDataAndSignOut()
        if (securityState.mustUpSyncEnrolmentRecords != null) {
            Simber.i("subject ids ${securityState.mustUpSyncEnrolmentRecords.subjectIds.size}")
            enrolmentRecordManager.upload(
                securityState.mustUpSyncEnrolmentRecords.id,
                securityState.mustUpSyncEnrolmentRecords.subjectIds
            )
        }
    }

    private suspend fun deleteLocalDataAndSignOut() {
        deleteLocalData()
        signOut()
    }

    private suspend fun deleteLocalData() {
        imageRepository.deleteStoredImages()
        enrolmentRecordManager.deleteAll()
    }

    private suspend fun signOut() {
        signerManager.signOut()
    }

}
