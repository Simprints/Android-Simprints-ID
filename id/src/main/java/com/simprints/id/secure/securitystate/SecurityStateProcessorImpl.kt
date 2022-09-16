package com.simprints.id.secure.securitystate

import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.enrolmentrecords.worker.EnrolmentRecordScheduler
import com.simprints.id.secure.SignerManager
import com.simprints.id.secure.models.SecurityState
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.logging.Simber

class SecurityStateProcessorImpl(
    private val imageRepository: ImageRepository,
    private val subjectRepository: SubjectRepository,
    private val enrolmentRecordScheduler: EnrolmentRecordScheduler,
    private val signerManager: SignerManager
) : SecurityStateProcessor {

    override suspend fun processSecurityState(securityState: SecurityState) {
        if (securityState.status.isCompromisedOrProjectEnded())
            deleteLocalDataAndSignOut()
        if (securityState.mustUpSyncEnrolmentRecords != null) {
            Simber.i("subject ids ${securityState.mustUpSyncEnrolmentRecords.subjectIds.size}")
            enrolmentRecordScheduler.upload(
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
        subjectRepository.deleteAll()
    }

    private suspend fun signOut() {
        signerManager.signOut()
    }

}
