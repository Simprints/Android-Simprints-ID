package com.simprints.feature.clientapi.usecases

import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration
import com.simprints.infra.events.EventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DeleteSessionEventsIfNeededUseCaseTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var eventRepository: EventRepository

    private lateinit var deleteUseCase: DeleteSessionEventsIfNeededUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        deleteUseCase = DeleteSessionEventsIfNeededUseCase(
            configRepository,
            eventRepository,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    @Test
    fun `deletes session events if data sync disabled`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } returns mockk {
            coEvery { synchronization.up.simprints.kind } returns UpSynchronizationConfiguration.UpSynchronizationKind.NONE
        }

        deleteUseCase("sessionId")

        coVerify { eventRepository.deleteEventScope("sessionId") }
    }


    @Test
    fun `does not delete session events if data sync enabled`() = runTest {

        coEvery { configRepository.getProjectConfiguration() } returns mockk {
            coEvery { synchronization.up.simprints.kind } returns UpSynchronizationConfiguration.UpSynchronizationKind.ALL
        }

        deleteUseCase("sessionId")

        coVerify(exactly = 0) { eventRepository.deleteEventScope("sessionId") }
    }
}
