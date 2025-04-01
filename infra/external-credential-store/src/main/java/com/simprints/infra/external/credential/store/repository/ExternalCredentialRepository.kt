package com.simprints.infra.external.credential.store.repository

import com.simprints.infra.external.credential.store.model.ExternalCredential

interface ExternalCredentialRepository<T : ExternalCredential> {
    suspend fun save(credential: T)
    suspend fun findByCredential(credential: T): T?
    suspend fun findBySubjectId(subjectId: String): T?
}
