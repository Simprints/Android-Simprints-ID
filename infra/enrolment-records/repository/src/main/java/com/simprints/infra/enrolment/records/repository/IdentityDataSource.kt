package com.simprints.infra.enrolment.records.repository

import com.simprints.core.domain.sample.Identity
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

interface IdentityDataSource {
    suspend fun count(
        query: SubjectQuery = SubjectQuery(),
        dataSource: BiometricDataSource = BiometricDataSource.Simprints,
    ): Int

    suspend fun loadIdentities(
        query: SubjectQuery,
        ranges: List<IntRange>,
        dataSource: BiometricDataSource,
        project: Project,
        scope: CoroutineScope,
        onCandidateLoaded: suspend () -> Unit,
    ): ReceiveChannel<List<Identity>>

    /**
     * Loads identities concurrently using the provided dispatcher and parallelism level.
     *
     */
}
