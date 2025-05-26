package com.simprints.infra.enrolment.records.repository

import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.repository.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

interface IdentityDataSource {
    suspend fun count(
        query: SubjectQuery = SubjectQuery(),
        dataSource: BiometricDataSource = BiometricDataSource.Simprints,
    ): Int

    suspend fun loadFingerprintIdentities(
        query: SubjectQuery,
        ranges: List<IntRange>,
        dataSource: BiometricDataSource,
        project: Project,
        scope: CoroutineScope,
        onCandidateLoaded: () -> Unit,
    ): ReceiveChannel<List<FingerprintIdentity>>

    suspend fun loadFaceIdentities(
        query: SubjectQuery,
        ranges: List<IntRange>,
        dataSource: BiometricDataSource,
        project: Project,
        scope: CoroutineScope,
        onCandidateLoaded: () -> Unit,
    ): ReceiveChannel<List<FaceIdentity>>

    /**
     * Loads identities concurrently using the provided dispatcher and parallelism level.
     *
     */
    fun <T> loadIdentitiesConcurrently(
        ranges: List<IntRange>,
        dispatcher: CoroutineDispatcher,
        parallelism: Int,
        scope: CoroutineScope,
        load: suspend (IntRange) -> List<T>,
    ): ReceiveChannel<List<T>> {
        val channel = Channel<List<T>>(parallelism)
        val semaphore = Semaphore(parallelism)
        scope.launch {
            ranges
                .map { range ->
                    launch {
                        semaphore.withPermit {
                            Simber.i("Loading range: $range")
                            channel.send(load(range))
                            Simber.i("Finished loading range: $range")
                        }
                    }
                }.joinAll()
            channel.close()
        }
        return channel
    }
}
