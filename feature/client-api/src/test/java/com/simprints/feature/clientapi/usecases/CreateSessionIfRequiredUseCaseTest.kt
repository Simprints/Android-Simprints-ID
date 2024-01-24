package com.simprints.feature.clientapi.usecases

import com.google.common.truth.Truth
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.clientapi.models.CommCareConstants
import com.simprints.feature.clientapi.models.LibSimprintsConstants
import com.simprints.feature.clientapi.models.OdkConstants
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.IntentParsingEvent
import com.simprints.infra.orchestration.data.ActionConstants
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class CreateSessionIfRequiredUseCaseTest {

    @MockK
    private lateinit var coreEventRepository: EventRepository

    @MockK
    private lateinit var timeHelper: TimeHelper

    private lateinit var useCase: CreateSessionIfRequiredUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { coreEventRepository.getCurrentSessionScope() } returns mockk {
            coEvery { id } returns SESSION_ID
        }

        useCase = CreateSessionIfRequiredUseCase(coreEventRepository, timeHelper)
    }

    @Test
    fun `Does not create session for confirm actions`() = runTest {
        useCase("${LibSimprintsConstants.PACKAGE_NAME}.${ActionConstants.ACTION_CONFIRM_IDENTITY}")

        coVerify(exactly = 0) { coreEventRepository.createSession() }
    }

    @Test
    fun `Does not create session for last biometric actions`() = runTest {
        useCase("${LibSimprintsConstants.PACKAGE_NAME}.${ActionConstants.ACTION_ENROL_LAST_BIOMETRICS}")

        coVerify(exactly = 0) { coreEventRepository.createSession() }
    }

    @Test
    fun `Creates session for LibSimprints flow actions`() = runTest {
        useCase("${LibSimprintsConstants.PACKAGE_NAME}.${ActionConstants.ACTION_ENROL}")

        coVerify {
            coreEventRepository.createSession()
            coreEventRepository.addOrUpdateEvent(withArg { Truth.assertThat(it).isInstanceOf(IntentParsingEvent::class.java) })
        }
    }

    @Test
    fun `Creates session for ODK flow actions`() = runTest {
        useCase("${OdkConstants.PACKAGE_NAME}.${ActionConstants.ACTION_ENROL}")

        coVerify {
            coreEventRepository.createSession()
            coreEventRepository.addOrUpdateEvent(withArg { Truth.assertThat(it).isInstanceOf(IntentParsingEvent::class.java) })
        }
    }

    @Test
    fun `Creates session for CommCare flow actions`() = runTest {
        useCase("${CommCareConstants.PACKAGE_NAME}.${ActionConstants.ACTION_ENROL}")

        coVerify {
            coreEventRepository.createSession()
            coreEventRepository.addOrUpdateEvent(withArg { Truth.assertThat(it).isInstanceOf(IntentParsingEvent::class.java) })
        }
    }

    companion object {

        private const val SESSION_ID = "sessionId"
    }
}
