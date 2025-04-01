package com.simprints.infra.external.credential.store.repository

import com.simprints.core.DispatcherIO
import com.simprints.infra.external.credential.store.datasource.local.QrExternalCredentialLocalDataSource
import com.simprints.infra.external.credential.store.model.ExternalCredential
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class QrExternalCredentialRepository @Inject constructor(
    private val localDataSource: QrExternalCredentialLocalDataSource,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) : ExternalCredentialRepository<ExternalCredential.QrCode> {

    override suspend fun save(credential: ExternalCredential.QrCode) = withContext(dispatcher) {
        localDataSource.save(credential)
    }

    override suspend fun findByCredential(credential: ExternalCredential.QrCode): ExternalCredential.QrCode? = withContext(dispatcher) {
        return@withContext localDataSource.findByCredential(credential)
    }

    override suspend fun findBySubjectId(subjectId: String): ExternalCredential.QrCode? = withContext(dispatcher) {
        return@withContext localDataSource.findBySubjectId(subjectId)
    }
}
