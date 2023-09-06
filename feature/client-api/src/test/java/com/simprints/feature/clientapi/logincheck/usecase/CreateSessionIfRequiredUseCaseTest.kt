package com.simprints.feature.clientapi.logincheck.usecase

import com.simprints.feature.clientapi.models.CommCareConstants
import com.simprints.feature.clientapi.models.IntegrationConstants
import com.simprints.feature.clientapi.models.LibSimprintsConstants
import com.simprints.feature.clientapi.models.OdkConstants
import com.simprints.feature.clientapi.session.ClientSessionManager
import com.simprints.infra.events.event.domain.models.IntentParsingEvent
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class CreateSessionIfRequiredUseCaseTest {

    @MockK
    lateinit var clientSessionManager: ClientSessionManager

    private lateinit var useCase: CreateSessionIfRequiredUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = CreateSessionIfRequiredUseCase(clientSessionManager)
    }

    @Test
    fun `Does not create session for confirm actions`() = runTest {
        useCase("${LibSimprintsConstants.PACKAGE_NAME}.${IntegrationConstants.ACTION_CONFIRM_IDENTITY}")

        coVerify(exactly = 0) { clientSessionManager.createSession(any()) }
    }

    @Test
    fun `Does not create session for last biometric actions`() = runTest {
        useCase("${LibSimprintsConstants.PACKAGE_NAME}.${IntegrationConstants.ACTION_ENROL_LAST_BIOMETRICS}")

        coVerify(exactly = 0) { clientSessionManager.createSession(any()) }
    }

    @Test
    fun `Creates session for LibSimprints flow actions`() = runTest {
        useCase("${LibSimprintsConstants.PACKAGE_NAME}.${IntegrationConstants.ACTION_ENROL}")

        coVerify {
            clientSessionManager.createSession(eq(IntentParsingEvent.IntentParsingPayload.IntegrationInfo.STANDARD))
        }
    }

    @Test
    fun `Creates session for ODK flow actions`() = runTest {
        useCase("${OdkConstants.PACKAGE_NAME}.${IntegrationConstants.ACTION_ENROL}")

        coVerify {
            clientSessionManager.createSession(eq(IntentParsingEvent.IntentParsingPayload.IntegrationInfo.ODK))
        }
    }

    @Test
    fun `Creates session for CommCare flow actions`() = runTest {
        useCase("${CommCareConstants.PACKAGE_NAME}.${IntegrationConstants.ACTION_ENROL}")

        coVerify {
            clientSessionManager.createSession(eq(IntentParsingEvent.IntentParsingPayload.IntegrationInfo.COMMCARE))
        }
    }
}
