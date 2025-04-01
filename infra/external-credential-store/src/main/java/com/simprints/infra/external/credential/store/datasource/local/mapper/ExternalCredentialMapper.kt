package com.simprints.infra.external.credential.store.datasource.local.mapper

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.enrolment.records.realm.store.models.QrExternalCredentialRealm
import com.simprints.infra.external.credential.store.model.ExternalCredential

@ExcludedFromGeneratedTestCoverageReports("Temporary external credential mapper")
object ExternalCredentialMapper {
    fun ExternalCredential.QrCode.toDB(): QrExternalCredentialRealm = QrExternalCredentialRealm().also {
        it.data = data
        it.subjectId = subjectId!!
    }

    fun QrExternalCredentialRealm.fromDb(): ExternalCredential.QrCode = ExternalCredential.QrCode(data = data, subjectId = subjectId)
}
