package com.simprints.infra.external.credential.store.repository

import com.simprints.core.DispatcherIO
import com.simprints.infra.external.credential.store.datasource.local.ExternalCredentialLocalDataSource
import com.simprints.infra.external.credential.store.model.ExternalCredential
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExternalCredentialRepository @Inject constructor(
    private val localDataSource: ExternalCredentialLocalDataSource,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) {

    suspend fun save(credential: ExternalCredential) = withContext(dispatcher) {
        // [MS-984] Deleting an existing records just in case. Currently, overrides are allowed (May 2025)
        deleteByCredential(credential.data)
        localDataSource.save(credential)
    }

    suspend fun deleteByCredential(credential: String) = withContext(dispatcher) {
        localDataSource.deleteByCredential(credential)
    }

    suspend fun findByCredential(credential: String): ExternalCredential? =
        withContext(dispatcher) {
            return@withContext localDataSource.findByCredential(credential)
        }

    suspend fun findBySubjectId(subjectId: String): ExternalCredential? = withContext(dispatcher) {
        return@withContext localDataSource.findBySubjectId(subjectId)
    }
}
