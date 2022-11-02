package com.simprints.id.activities.orchestrator

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.callback.*
import com.simprints.id.domain.moduleapi.app.responses.*
import com.simprints.id.domain.moduleapi.app.responses.entities.*
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import org.junit.Before
import org.junit.Rule

import org.junit.Test
import java.util.*

class OrchestratorEventsHelperImplTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var timeHelper: TimeHelper


    private lateinit var helper: OrchestratorEventsHelperImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        val scope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher)
        helper = OrchestratorEventsHelperImpl(
            eventRepository,
            timeHelper,
            scope
        )


        coEvery { eventRepository.addOrUpdateEvent(any()) } just Runs
    }

    @Test
    fun addCallbackEventInSessions_shouldAddEnrolmentCallbackEvent() {
        val uid = UUID.randomUUID().toString()
        val appResponse = AppEnrolResponse(uid)
        val eventSlot = CapturingSlot<EnrolmentCallbackEvent>()

        helper.addCallbackEventInSessions(appResponse)

        coVerify { eventRepository.addOrUpdateEvent(capture(eventSlot)) }
        assertThat(eventSlot.captured.payload.guid).isEqualTo(uid)
    }

    @Test
    fun addCallbackEventInSessions_shouldAddIdentificationCallbackEvent() {
        val uid = UUID.randomUUID().toString()
        val list: List<MatchResult> = listOf(
            MatchResult(UUID.randomUUID().toString(), 90, Tier.TIER_4, MatchConfidence.HIGH),
            MatchResult(UUID.randomUUID().toString(), 66, Tier.TIER_3, MatchConfidence.MEDIUM)
        )
        val appResponse = AppIdentifyResponse(list, uid)
        val eventSlot = CapturingSlot<IdentificationCallbackEvent>()

        helper.addCallbackEventInSessions(appResponse)

        coVerify { eventRepository.addOrUpdateEvent(capture(eventSlot)) }
        assertThat(eventSlot.captured.payload.sessionId).isEqualTo(uid)
        eventSlot.captured.payload.scores.forEachIndexed { index, comparisonScore ->
            val matchResult = list[index]
            assertThat(comparisonScore.guid).isEqualTo(matchResult.guidFound)
            assertThat(comparisonScore.confidence).isEqualTo(matchResult.confidence)
            assertThat(comparisonScore.tier.name).isEqualTo(matchResult.tier.name)
        }
    }


    @Test
    fun addCallbackEventInSessions_shouldAddRefusalCallbackEvent() {
        val appResponse = AppRefusalFormResponse(
            RefusalFormAnswer(RefusalFormReason.APP_NOT_WORKING))
        val eventSlot = CapturingSlot<RefusalCallbackEvent>()

        helper.addCallbackEventInSessions(appResponse)

        coVerify { eventRepository.addOrUpdateEvent(capture(eventSlot)) }
        assertThat(eventSlot.captured.payload.reason).isEqualTo(appResponse.answer.reason.name)
    }


    @Test
    fun addCallbackEventInSessions_shouldAddVerificationCallbackEvent() {
        val matchResult = MatchResult(
            UUID.randomUUID().toString(),
            90,
            Tier.TIER_4,
            MatchConfidence.HIGH
        )
        val appResponse = AppVerifyResponse(matchResult)
        val eventSlot = CapturingSlot<VerificationCallbackEvent>()

        helper.addCallbackEventInSessions(appResponse)

        coVerify { eventRepository.addOrUpdateEvent(capture(eventSlot)) }
        with(eventSlot.captured.payload.score) {
            assertThat(guid).isEqualTo(matchResult.guidFound)
            assertThat(confidence).isEqualTo(matchResult.confidence)
            assertThat(tier.name).isEqualTo(matchResult.tier.name)
        }
    }

    @Test
    fun addCallbackEventInSessions_shouldAddConfirmationCallbackEvent() {
        val appResponse = AppConfirmationResponse(true)
        val eventSlot = CapturingSlot<ConfirmationCallbackEvent>()

        helper.addCallbackEventInSessions(appResponse)

        coVerify { eventRepository.addOrUpdateEvent(capture(eventSlot)) }
        assertThat(eventSlot.captured.payload.identificationOutcome)
            .isEqualTo(appResponse.identificationOutcome)
    }

    @Test
    fun addCallbackEventInSessions_shouldAddErrorCallbackEvent() {
        val appResponse = AppErrorResponse(AppErrorResponse.Reason.BACKEND_MAINTENANCE_ERROR)
        val eventSlot = CapturingSlot<ErrorCallbackEvent>()

        helper.addCallbackEventInSessions(appResponse)

        coVerify { eventRepository.addOrUpdateEvent(capture(eventSlot)) }
        assertThat(eventSlot.captured.payload.reason.name).isEqualTo(appResponse.reason.name)
    }
}
