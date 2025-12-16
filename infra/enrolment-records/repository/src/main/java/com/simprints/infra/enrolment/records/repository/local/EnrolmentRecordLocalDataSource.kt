package com.simprints.infra.enrolment.records.repository.local

import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.CandidateRecordDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery

interface EnrolmentRecordLocalDataSource : CandidateRecordDataSource {
    suspend fun load(query: SubjectQuery): List<Subject>

    suspend fun delete(queries: List<SubjectQuery>)

    suspend fun deleteAll()

    suspend fun performActions(
        actions: List<SubjectAction>,
        project: Project,
    )

    suspend fun getLocalDBInfo(): String

    suspend fun getAllSubjectIds(): List<String>

    suspend fun closeOpenDbConnection()
}
