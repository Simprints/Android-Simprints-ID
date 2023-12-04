package com.simprints.infra.enrolment.records.sync

import com.simprints.infra.enrolment.records.store.domain.models.Subject
import com.simprints.infra.enrolment.records.store.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery

interface EnrolmentRecordManager {
    fun upload(id: String, subjectIds: List<String>)

    suspend fun uploadRecords(subjectIds: List<String>)
    suspend fun load(query: SubjectQuery): List<Subject>
    suspend fun delete(queries: List<SubjectQuery>)
    suspend fun deleteAll()
    suspend fun count(query: SubjectQuery = SubjectQuery()): Int

    suspend fun performActions(actions: List<SubjectAction>)
}
