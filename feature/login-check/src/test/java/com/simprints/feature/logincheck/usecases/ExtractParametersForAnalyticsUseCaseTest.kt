package com.simprints.feature.logincheck.usecases

import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.logging.Simber
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

internal class ExtractParametersForAnalyticsUseCaseTest {

    @MockK
    private lateinit var eventRepository: SessionEventRepository

    private lateinit var useCase: ExtractParametersForAnalyticsUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { eventRepository.getCurrentSessionScope() } returns mockk {
            every { id } returns "sessionId"
        }

        mockkObject(Simber)

        useCase = ExtractParametersForAnalyticsUseCase("deviceId", eventRepository)
    }

    @After
    fun cleanUp() {
        unmockkObject(Simber)
    }

    @Test
    fun `Logs analytics keys in flow actions`() = runTest {
        useCase(ActionFactory.getFlowRequest())

        verify {
            Simber.i(ActionFactory.MOCK_USER_ID.toString())
            Simber.i(ActionFactory.MOCK_PROJECT_ID)
            Simber.i(ActionFactory.MOCK_MODULE_ID.toString())
            Simber.i("deviceId")
            Simber.i("sessionId")
        }
    }


    @Test
    fun `Does not log analytics keys in follow up actions`() = runTest {
        useCase(ActionFactory.getFolowUpRequest())

        verify(exactly = 0) {
            Simber.i(ActionFactory.MOCK_USER_ID.toString())
            Simber.i(ActionFactory.MOCK_PROJECT_ID)
            Simber.i(ActionFactory.MOCK_MODULE_ID.toString())
            Simber.i("deviceId")
        }
    }
}
