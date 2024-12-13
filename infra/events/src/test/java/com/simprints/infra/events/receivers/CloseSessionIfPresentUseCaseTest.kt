package com.simprints.infra.events.receivers

import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CloseSessionIfPresentUseCaseTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var eventRepository: SessionEventRepository

    private lateinit var useCase: CloseSessionIfPresentUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = CloseSessionIfPresentUseCase(
            eventRepository,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    @Test
    fun `when session is present, it should be closed`() = runTest {
        coEvery { eventRepository.hasOpenSession() } returns true

        useCase()

        coVerify { eventRepository.closeCurrentSession(any()) }
    }

    @Test
    fun `when session is not present, nothing should happen`() = runTest {
        coEvery { eventRepository.hasOpenSession() } returns false

        useCase()

        coVerify(exactly = 0) { eventRepository.closeCurrentSession(any()) }
    }
}
