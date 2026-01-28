package com.simprints.infra.sync.config.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.sync.SyncCommands
import com.simprints.infra.sync.SyncResponse
import com.simprints.infra.sync.config.testtools.projectConfiguration
import com.simprints.infra.sync.config.testtools.simprintsUpSyncConfigurationConfiguration
import com.simprints.infra.sync.config.testtools.synchronizationConfiguration
import com.simprints.infra.sync.usecase.SyncUseCase
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RescheduleWorkersIfConfigChangedUseCaseTest {
    @MockK
    private lateinit var sync: SyncUseCase

    private lateinit var useCase: RescheduleWorkersIfConfigChangedUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { sync(any()) } returns noopSyncResponse()

        useCase = RescheduleWorkersIfConfigChangedUseCase(sync)
    }

    @Test
    fun `should not reschedule image upload when unmetered connection flag not changes`() = runTest {
        useCase(
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    up = synchronizationConfiguration.up.copy(
                        simprints = simprintsUpSyncConfigurationConfiguration.copy(
                            imagesRequireUnmeteredConnection = true,
                        ),
                    ),
                ),
            ),
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    up = synchronizationConfiguration.up.copy(
                        simprints = simprintsUpSyncConfigurationConfiguration.copy(
                            imagesRequireUnmeteredConnection = true,
                        ),
                    ),
                ),
            ),
        )

        verify(exactly = 0) { sync(any()) }
    }

    @Test
    fun `should reschedule image upload when unmetered connection flag changes`() = runTest {
        val syncCommandJob = Job()
        every { sync(any()) } returns SyncResponse(
            syncCommandJob = syncCommandJob,
            syncStatusFlow = MutableStateFlow(mockk(relaxed = true)),
        )

        val useCaseJob = async {
            useCase(
                projectConfiguration.copy(
                    synchronization = synchronizationConfiguration.copy(
                        up = synchronizationConfiguration.up.copy(
                            simprints = simprintsUpSyncConfigurationConfiguration.copy(
                                imagesRequireUnmeteredConnection = false,
                            ),
                        ),
                    ),
                ),
                projectConfiguration.copy(
                    synchronization = synchronizationConfiguration.copy(
                        up = synchronizationConfiguration.up.copy(
                            simprints = simprintsUpSyncConfigurationConfiguration.copy(
                                imagesRequireUnmeteredConnection = true,
                            ),
                        ),
                    ),
                ),
            )
        }

        runCurrent()
        assertThat(useCaseJob.isCompleted).isFalse()

        syncCommandJob.complete()
        runCurrent()
        useCaseJob.await()

        verify { sync(SyncCommands.Schedule.Images.start()) }
        assertThat(useCaseJob.isCompleted).isTrue()
    }

    private fun noopSyncResponse() = SyncResponse(
        syncCommandJob = Job().apply { complete() },
        syncStatusFlow = MutableStateFlow(mockk(relaxed = true)),
    )
}
