package com.simprints.infra.enrolment.records.store.local

import com.simprints.infra.enrolment.records.store.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.store.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.store.domain.models.Subject
import com.simprints.infra.enrolment.records.store.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery

interface EnrolmentRecordLocalDataSource {

    suspend fun load(query: SubjectQuery): List<Subject>
    suspend fun loadFingerprintIdentities(query: SubjectQuery, range: IntRange): List<FingerprintIdentity>
    suspend fun loadFaceIdentities(query: SubjectQuery, range: IntRange): List<FaceIdentity>

    suspend fun delete(queries: List<SubjectQuery>)
    suspend fun deleteAll()
    suspend fun count(query: SubjectQuery = SubjectQuery()): Int

    suspend fun performActions(actions: List<SubjectAction>)
}