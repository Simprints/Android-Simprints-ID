package com.simprints.infra.enrolment.records

import com.simprints.infra.enrolment.records.domain.models.*
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

interface EnrolmentRecordManager {
    fun upload(id: String, subjectIds: List<String>)

    suspend fun loadFaceIdentities(query: Serializable): Flow<FaceIdentity>
    suspend fun loadFingerprintIdentities(query: Serializable): Flow<FingerprintIdentity>
    suspend fun uploadRecords(subjectIds: List<String>)
    suspend fun load(query: SubjectQuery): Flow<Subject>
    suspend fun delete(queries: List<SubjectQuery>)
    suspend fun deleteAll()
    suspend fun count(query: SubjectQuery = SubjectQuery()): Int

    suspend fun performActions(actions: List<SubjectAction>)
}
