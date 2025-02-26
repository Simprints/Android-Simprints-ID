package com.simprints.infra.enrolment.records.store.local

import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.store.IdentityDataSource
import com.simprints.infra.enrolment.records.store.domain.models.Subject
import com.simprints.infra.enrolment.records.store.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery

interface EnrolmentRecordLocalDataSource : IdentityDataSource {
    suspend fun load(query: SubjectQuery): List<Subject>

    suspend fun delete(queries: List<SubjectQuery>)

    suspend fun deleteAll()

    suspend fun performActions(actions: List<SubjectAction>, project: Project)
}
