package com.simprints.feature.orchestrator.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.orchestrator.model.responses.AppConfirmationResponse
import com.simprints.feature.orchestrator.model.responses.AppEnrolResponse
import com.simprints.feature.orchestrator.model.responses.AppErrorResponse
import com.simprints.feature.orchestrator.model.responses.AppIdentifyResponse
import com.simprints.feature.orchestrator.model.responses.AppMatchResult
import com.simprints.feature.orchestrator.model.responses.AppRefusalResponse
import com.simprints.feature.orchestrator.model.responses.AppVerifyResponse
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.callback.ConfirmationCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.EnrolmentCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.IdentificationCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.RefusalCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.VerificationCallbackEvent
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.core.domain.response.AppMatchConfidence
import com.simprints.core.domain.response.AppResponseTier
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.CoroutineScope
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddCallbackEventUseCaseTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var timeHelper: TimeHelper

    private lateinit var useCase: AddCallbackEventUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = AddCallbackEventUseCase(
            eventRepository,
            timeHelper,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher)
        )

        coEvery { eventRepository.addOrUpdateEvent(any()) } just Runs
    }

    @Test
    fun `adds event for enrol response`() {
        useCase(AppEnrolResponse("guid"))

        coVerify {
            eventRepository.addOrUpdateEvent(withArg {
                assertThat(it).isInstanceOf(EnrolmentCallbackEvent::class.java)
            })
        }
    }

    @Test
    fun `adds event for identification response`() {
        useCase(AppIdentifyResponse(
            listOf(AppMatchResult("guid", 0, AppResponseTier.TIER_1, AppMatchConfidence.HIGH)),
            "sessionId"
        ))

        coVerify {
            eventRepository.addOrUpdateEvent(withArg {
                assertThat(it).isInstanceOf(IdentificationCallbackEvent::class.java)
            })
        }
    }

    @Test
    fun `adds event for verification response`() {
        useCase(AppVerifyResponse(AppMatchResult("guid", 0, AppResponseTier.TIER_1, AppMatchConfidence.HIGH)))

        coVerify {
            eventRepository.addOrUpdateEvent(withArg {
                assertThat(it).isInstanceOf(VerificationCallbackEvent::class.java)
            })
        }
    }

    @Test
    fun `adds event for confirmation response`() {
        useCase(AppConfirmationResponse(true))

        coVerify {
            eventRepository.addOrUpdateEvent(withArg {
                assertThat(it).isInstanceOf(ConfirmationCallbackEvent::class.java)
            })
        }
    }

    @Test
    fun `adds event for refusal response`() {
        useCase(AppRefusalResponse("reason", "extra"))

        coVerify {
            eventRepository.addOrUpdateEvent(withArg {
                assertThat(it).isInstanceOf(RefusalCallbackEvent::class.java)
            })
        }
    }

    @Test
    fun `adds event for error response`() {
        useCase(AppErrorResponse(AppErrorReason.UNEXPECTED_ERROR))

        coVerify {
            eventRepository.addOrUpdateEvent(withArg {
                assertThat(it).isInstanceOf(ErrorCallbackEvent::class.java)
            })
        }
    }
}
