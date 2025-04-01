package com.simprints.infra.external.credential.store.datasource.local

import com.simprints.infra.enrolment.records.realm.store.RealmWrapper
import com.simprints.infra.enrolment.records.realm.store.models.QrExternalCredentialRealm
import com.simprints.infra.external.credential.store.datasource.local.mapper.ExternalCredentialMapper.fromDb
import com.simprints.infra.external.credential.store.datasource.local.mapper.ExternalCredentialMapper.toDB
import com.simprints.infra.external.credential.store.model.ExternalCredential
import javax.inject.Inject

class QrExternalCredentialLocalDataSource @Inject constructor(
    private val realmWrapper: RealmWrapper,
) : ExternalCredentialLocalDataSource<ExternalCredential.QrCode> {
    override suspend fun save(credential: ExternalCredential.QrCode) = realmWrapper.writeRealm { realm ->
        realm.copyToRealm(credential.toDB())
    }

    override suspend fun findByCredential(credential: ExternalCredential.QrCode): ExternalCredential.QrCode? =
        realmWrapper.readRealm { realm ->
            return@readRealm realm
                .query(QrExternalCredentialRealm::class, "data == $0", credential.data)
                .first()
                .find()
                ?.fromDb()
        }

    override suspend fun findBySubjectId(subjectId: String): ExternalCredential.QrCode? =
        realmWrapper.readRealm { realm ->
            return@readRealm realm
                .query(QrExternalCredentialRealm::class, "subjectId == $0", subjectId)
                .first()
                .find()
                ?.fromDb()
        }
}
