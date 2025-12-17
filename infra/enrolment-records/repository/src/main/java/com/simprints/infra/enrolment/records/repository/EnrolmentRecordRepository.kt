package com.simprints.infra.enrolment.records.repository

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.enrolment.records.repository.local.EnrolmentRecordLocalDataSource

@ExcludedFromGeneratedTestCoverageReports("This is an interface with no logic")
interface EnrolmentRecordRepository : EnrolmentRecordLocalDataSource {
    suspend fun uploadRecords(subjectIds: List<String>)

    suspend fun tokenizeExistingRecords(project: Project)

    override suspend fun count(
        query: EnrolmentRecordQuery,
        dataSource: BiometricDataSource,
    ): Int
}
