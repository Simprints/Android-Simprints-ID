package com.simprints.feature.logincheck.usecases

import com.simprints.infra.logging.Simber
import io.mockk.MockKAnnotations
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

internal class ExtractParametersForAnalyticsUseCaseTest {
    private lateinit var useCase: ExtractParametersForAnalyticsUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        mockkObject(Simber)

        useCase = ExtractParametersForAnalyticsUseCase("deviceId")
    }

    @After
    fun cleanUp() {
        unmockkObject(Simber)
    }

    @Test
    fun `Logs analytics keys in flow actions`() = runTest {
        useCase(ActionFactory.getFlowRequest())

        verify {
            Simber.setUserProperty(any(), ActionFactory.MOCK_USER_ID.toString())
            Simber.setUserProperty(any(), ActionFactory.MOCK_PROJECT_ID)
            Simber.setUserProperty(any(), ActionFactory.MOCK_MODULE_ID.toString())
            Simber.setUserProperty(any(), "deviceId")
        }
    }

    @Test
    fun `Does not log analytics keys in follow up actions`() = runTest {
        useCase(ActionFactory.getFolowUpRequest())

        verify(exactly = 0) {
            Simber.setUserProperty(any(), any())
        }
    }
}
