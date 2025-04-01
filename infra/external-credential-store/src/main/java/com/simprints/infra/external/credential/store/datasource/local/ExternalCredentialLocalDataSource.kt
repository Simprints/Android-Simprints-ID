package com.simprints.infra.external.credential.store.datasource.local

import com.simprints.infra.external.credential.store.model.ExternalCredential

interface ExternalCredentialLocalDataSource <T: ExternalCredential> {
    suspend fun save(credential: T)
    suspend fun findByCredential(credential: T): T?
    suspend fun findBySubjectId(subjectId: String): T?
}
