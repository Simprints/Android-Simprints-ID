package com.simprints.infra.enrolment.records.store.local

import com.simprints.infra.enrolment.records.store.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery

interface FingerprintIdentityLocalDataSource {

    suspend fun loadFingerprintIdentities(query: SubjectQuery): List<FingerprintIdentity>
}
