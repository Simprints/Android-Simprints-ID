package com.simprints.infra.enrolment.records.store

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.store.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.store.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.store.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.store.local.EnrolmentRecordLocalDataSource

@ExcludedFromGeneratedTestCoverageReports("This is an interface with no logic")
interface EnrolmentRecordRepository : EnrolmentRecordLocalDataSource {
    suspend fun uploadRecords(subjectIds: List<String>)
    suspend fun tokenizeExistingRecords(project: Project)

    suspend fun count(
        query: SubjectQuery = SubjectQuery(),
        dataSource: BiometricDataSource = BiometricDataSource.SIMPRINTS
    ): Int

    suspend fun loadFingerprintIdentities(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource = BiometricDataSource.SIMPRINTS
    ): List<FingerprintIdentity>

    suspend fun loadFaceIdentities(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource = BiometricDataSource.SIMPRINTS
    ): List<FaceIdentity>
}
