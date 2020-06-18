package com.simprints.id.secure.securitystate

import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.secure.SignerManager
import com.simprints.id.secure.models.SecurityState

class SecurityStateProcessorImpl(
    private val imageRepository: ImageRepository,
    private val subjectRepository: SubjectRepository,
    private val sessionRepository: SessionRepository,
    private val signerManager: SignerManager
) : SecurityStateProcessor {

    override suspend fun processSecurityState(securityState: SecurityState) {
        if (securityState.status.isCompromisedOrProjectEnded())
            deleteLocalDataAndSignOut()
    }

    private suspend fun deleteLocalDataAndSignOut() {
        deleteLocalData()
        signOut()
    }

    private suspend fun deleteLocalData() {
        imageRepository.deleteStoredImages()
        subjectRepository.deleteAll()
        sessionRepository.deleteAllFromLocal()
    }

    private suspend fun signOut() {
        signerManager.signOut()
    }

    private fun SecurityState.Status.isCompromisedOrProjectEnded(): Boolean {
        return this == SecurityState.Status.COMPROMISED
            || this == SecurityState.Status.PROJECT_ENDED
    }

}
