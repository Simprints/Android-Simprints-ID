package com.simprints.infra.sync.config.usecase

import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.sync.EventCounts
import com.simprints.infra.sync.usecase.CountEventsUseCase
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class HandleProjectStateUseCaseTest {
    @MockK
    private lateinit var countEvents: CountEventsUseCase

    @MockK
    private lateinit var logoutUseCase: LogoutUseCase

    private lateinit var useCase: HandleProjectStateUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = HandleProjectStateUseCase(
            countEvents = countEvents,
            logoutUseCase = logoutUseCase,
        )
    }

    @Test
    fun `Fully logs out when project has ended`() = runTest {
        every { countEvents.invoke() } returns flowOf(
            EventCounts(
                download = 0,
                isDownloadLowerBound = false,
                upload = 0,
                uploadEnrolmentV2 = 0,
                uploadEnrolmentV4 = 0,
            ),
        )

        useCase(ProjectState.PROJECT_ENDED)

        coVerify { logoutUseCase.invoke() }
    }

    @Test
    fun `Logs out when project has ending and no items to upload`() = runTest {
        every { countEvents.invoke() } returns flowOf(
            EventCounts(
                download = 0,
                isDownloadLowerBound = false,
                upload = 0,
                uploadEnrolmentV2 = 0,
                uploadEnrolmentV4 = 0,
            ),
        )

        useCase(ProjectState.PROJECT_ENDING)

        coVerify { logoutUseCase.invoke() }
    }

    @Test
    fun `Does not logs out when project has ending and has items to upload`() = runTest {
        every { countEvents.invoke() } returns flowOf(
            EventCounts(
                download = 0,
                isDownloadLowerBound = false,
                upload = 5,
                uploadEnrolmentV2 = 0,
                uploadEnrolmentV4 = 0,
            ),
        )

        useCase(ProjectState.PROJECT_ENDING)

        coVerify(exactly = 0) { logoutUseCase.invoke() }
    }

    @Test
    fun `Does not logs out when project is running`() = runTest {
        every { countEvents.invoke() } returns flowOf(
            EventCounts(
                download = 0,
                isDownloadLowerBound = false,
                upload = 0,
                uploadEnrolmentV2 = 0,
                uploadEnrolmentV4 = 0,
            ),
        )

        useCase(ProjectState.RUNNING)

        coVerify(exactly = 0) { logoutUseCase.invoke() }
    }
}
