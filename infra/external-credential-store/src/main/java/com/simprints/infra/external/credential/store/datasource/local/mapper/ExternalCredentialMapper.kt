package com.simprints.infra.external.credential.store.datasource.local.mapper

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.enrolment.records.realm.store.models.ExternalCredentialRealm
import com.simprints.infra.external.credential.store.model.ExternalCredential

@ExcludedFromGeneratedTestCoverageReports("Temporary external credential mapper")
object ExternalCredentialMapper {
    fun ExternalCredential.toDB(): ExternalCredentialRealm = ExternalCredentialRealm().also {
        it.data = data
        it.subjectId = subjectId!!
    }

    fun ExternalCredentialRealm.fromDb(): ExternalCredential = ExternalCredential(data = data, subjectId = subjectId)
}
