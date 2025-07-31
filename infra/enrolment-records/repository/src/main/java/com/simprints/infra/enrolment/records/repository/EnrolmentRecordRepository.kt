package com.simprints.infra.enrolment.records.repository

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.repository.local.EnrolmentRecordLocalDataSource

@ExcludedFromGeneratedTestCoverageReports("This is an interface with no logic")
interface EnrolmentRecordRepository : EnrolmentRecordLocalDataSource {
    suspend fun uploadRecords(subjectIds: List<String>)

    suspend fun tokenizeExistingRecords(project: Project)

    /**
     * Sync CommCare case cache with the current state of cases in CommCare.
     * This method should be called after syncing from CommCare to ensure that cases
     * no longer present in CommCare are removed from the cache.
     * @param currentCommCareCaseIds Set of case IDs currently present in CommCare
     */
    suspend fun syncCommCareCaseCache(currentCommCareCaseIds: Set<String>)

    override suspend fun count(
        query: SubjectQuery,
        dataSource: BiometricDataSource,
    ): Int
}
