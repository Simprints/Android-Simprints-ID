package com.simprints.infra.config.sync.usecase

import com.simprints.infra.config.sync.testtools.projectConfiguration
import com.simprints.infra.config.sync.testtools.synchronizationConfiguration
import com.simprints.infra.images.ImageUpSyncScheduler
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RescheduleWorkersIfConfigChangedUseCaseTest {

    @MockK
    private lateinit var imageUpSyncScheduler: ImageUpSyncScheduler

    private lateinit var useCase: RescheduleWorkersIfConfigChangedUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = RescheduleWorkersIfConfigChangedUseCase(imageUpSyncScheduler)
    }

    @Test
    fun `should not reschedule image upload when unmetered connection flag not changes`() =
        runTest {
            useCase(
                projectConfiguration.copy(
                    synchronization = synchronizationConfiguration.copy(
                        up = synchronizationConfiguration.up.copy(imagesRequireUnmeteredConnection = true)
                    )
                ),
                projectConfiguration.copy(
                    synchronization = synchronizationConfiguration.copy(
                        up = synchronizationConfiguration.up.copy(imagesRequireUnmeteredConnection = true)
                    )
                ),
            )

            coVerify(exactly = 0) { imageUpSyncScheduler.rescheduleImageUpSync() }
        }

    @Test
    fun `should reschedule image upload when unmetered connection flag changes`() =
        runTest {
            useCase(
                projectConfiguration.copy(
                    synchronization = synchronizationConfiguration.copy(
                        up = synchronizationConfiguration.up.copy(imagesRequireUnmeteredConnection = false)
                    )
                ),
                projectConfiguration.copy(
                    synchronization = synchronizationConfiguration.copy(
                        up = synchronizationConfiguration.up.copy(imagesRequireUnmeteredConnection = true)
                    )
                ),
            )

            coVerify { imageUpSyncScheduler.rescheduleImageUpSync() }
        }

}
