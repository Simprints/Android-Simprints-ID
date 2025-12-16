package com.simprints.infra.enrolment.records.repository.local

import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.CandidateRecordDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordAction
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.enrolment.records.repository.domain.models.Subject

interface EnrolmentRecordLocalDataSource : CandidateRecordDataSource {
    suspend fun load(query: EnrolmentRecordQuery): List<Subject>

    suspend fun delete(queries: List<EnrolmentRecordQuery>)

    suspend fun deleteAll()

    suspend fun performActions(
        actions: List<EnrolmentRecordAction>,
        project: Project,
    )

    suspend fun getLocalDBInfo(): String

    suspend fun getAllSubjectIds(): List<String>

    suspend fun closeOpenDbConnection()
}
