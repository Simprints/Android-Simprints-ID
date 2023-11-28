package com.simprints.infra.enrolment.records.store.local

import com.simprints.infra.enrolment.records.store.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery

interface FaceIdentityLocalDataSource {

    suspend fun loadFaceIdentities(query: SubjectQuery): List<FaceIdentity>
}
