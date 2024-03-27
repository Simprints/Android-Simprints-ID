package com.simprints.infra.enrolment.records.store

import com.simprints.infra.enrolment.records.store.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.store.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery

interface IdentityDataSource {

    suspend fun count(query: SubjectQuery = SubjectQuery()): Int
    suspend fun loadFingerprintIdentities(query: SubjectQuery, range: IntRange): List<FingerprintIdentity>
    suspend fun loadFaceIdentities(query: SubjectQuery, range: IntRange): List<FaceIdentity>
}