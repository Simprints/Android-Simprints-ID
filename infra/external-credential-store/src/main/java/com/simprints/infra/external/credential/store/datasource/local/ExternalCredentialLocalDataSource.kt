package com.simprints.infra.external.credential.store.datasource.local

import com.simprints.infra.enrolment.records.realm.store.RealmWrapper
import com.simprints.infra.enrolment.records.realm.store.models.ExternalCredentialRealm
import com.simprints.infra.external.credential.store.datasource.local.mapper.ExternalCredentialMapper.fromDb
import com.simprints.infra.external.credential.store.datasource.local.mapper.ExternalCredentialMapper.toDB
import com.simprints.infra.external.credential.store.model.ExternalCredential
import io.realm.kotlin.ext.query
import javax.inject.Inject

class ExternalCredentialLocalDataSource @Inject constructor(
    private val realmWrapper: RealmWrapper,
) {
    suspend fun save(credential: ExternalCredential) = realmWrapper.writeRealm { realm ->
        realm.copyToRealm(credential.toDB())
    }

    suspend fun findByCredential(credential: String): ExternalCredential? =
        realmWrapper.readRealm { realm ->
            return@readRealm realm
                .query(ExternalCredentialRealm::class, "data == $0", credential)
                .first()
                .find()
                ?.fromDb()
        }

    suspend fun findBySubjectId(subjectId: String): List<ExternalCredential> =
        realmWrapper.readRealm { realm ->
            return@readRealm realm
                .query(ExternalCredentialRealm::class, "subjectId == $0", subjectId)
                .find()
                .map { it.fromDb() }
        }

    suspend fun deleteByCredential(credential: String) = realmWrapper.writeRealm { realm ->
        realm.query(ExternalCredentialRealm::class, "data == $0", credential)
            .first()
            .find()
            ?.let {
                realm.delete(it)
            }
    }
}
