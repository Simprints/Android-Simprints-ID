package com.simprints.infra.enrolment.records.repository

import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.CandidateRecordBatch
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow

interface CandidateRecordDataSource {
    suspend fun count(
        query: EnrolmentRecordQuery = EnrolmentRecordQuery(),
        dataSource: BiometricDataSource = BiometricDataSource.Simprints,
    ): Int

    fun observeCount(
        query: EnrolmentRecordQuery = EnrolmentRecordQuery(),
        dataSource: BiometricDataSource = BiometricDataSource.Simprints,
    ): Flow<Int>

    /**
     * Loads records concurrently using the provided dispatcher and parallelism level.
     */
    suspend fun loadCandidateRecords(
        query: EnrolmentRecordQuery,
        ranges: List<IntRange>,
        dataSource: BiometricDataSource,
        project: Project,
        scope: CoroutineScope,
        onCandidateLoaded: suspend () -> Unit,
    ): ReceiveChannel<CandidateRecordBatch>
}
