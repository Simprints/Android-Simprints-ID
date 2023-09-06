package com.simprints.feature.clientapi.logincheck.usecase

import com.simprints.feature.clientapi.mappers.request.requestFactories.ConfirmIdentityActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.RequestActionFactory
import com.simprints.feature.clientapi.session.ClientSessionManager
import com.simprints.infra.logging.Simber
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

internal class ExtractParametersForAnalyticsUseCaseTest {

    @MockK
    private lateinit var clientSessionManager: ClientSessionManager

    private lateinit var useCase: ExtractParametersForAnalyticsUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { clientSessionManager.getCurrentSessionId() } returns "sessionId"

        mockkObject(Simber)

        useCase = ExtractParametersForAnalyticsUseCase("deviceId", clientSessionManager)
    }

    @After
    fun cleanUp() {
        unmockkObject(Simber)
    }

    @Test
    fun `Logs analytics keys in flow actions`() = runTest {
        useCase(EnrolActionFactory.getValidSimprintsRequest())

        verify {
            Simber.i(RequestActionFactory.MOCK_USER_ID)
            Simber.i(RequestActionFactory.MOCK_PROJECT_ID)
            Simber.i(RequestActionFactory.MOCK_MODULE_ID)
            Simber.i("deviceId")
            Simber.i("sessionId")
        }
    }


    @Test
    fun `Does not log analytics keys in follow up actions`() = runTest {
        useCase(ConfirmIdentityActionFactory.getValidSimprintsRequest())

        verify(exactly = 0) {
            Simber.i(RequestActionFactory.MOCK_USER_ID)
            Simber.i(RequestActionFactory.MOCK_PROJECT_ID)
            Simber.i(RequestActionFactory.MOCK_MODULE_ID)
            Simber.i("deviceId")
        }
    }
}
