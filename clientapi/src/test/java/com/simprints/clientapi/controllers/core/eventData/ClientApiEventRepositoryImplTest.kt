package com.simprints.clientapi.controllers.core.eventData

import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManager
import com.simprints.clientapi.data.sharedpreferences.canCoSyncAllData
import com.simprints.clientapi.data.sharedpreferences.canCoSyncAnalyticsData
import com.simprints.clientapi.data.sharedpreferences.canCoSyncBiometricData
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.INVALID_PROJECT_ID
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.NFC_NOT_ENABLED
import com.simprints.eventsystem.event.domain.models.ArtificialTerminationEvent
import com.simprints.eventsystem.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason
import com.simprints.eventsystem.event.domain.models.EnrolmentEventV2
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.IntentParsingEvent
import com.simprints.eventsystem.event.domain.models.InvalidIntentEvent
import com.simprints.eventsystem.event.domain.models.PersonCreationEvent
import com.simprints.eventsystem.event.domain.models.SuspiciousIntentEvent
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.eventsystem.event.domain.models.session.SessionCaptureEvent
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID
import com.simprints.eventsystem.event.domain.models.IntentParsingEvent.IntentParsingPayload.IntegrationInfo as CoreIntegrationInfo

@ExperimentalCoroutinesApi
class ClientApiEventRepositoryImplTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK(relaxed = true)
    lateinit var coreEventEventsMgrMock: EventRepository

    @MockK(relaxed = true)
    lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var clientSessionEventsMgr: ClientApiSessionEventsManager

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        clientSessionEventsMgr = ClientApiSessionEventsManagerImpl(
            coreEventEventsMgrMock,
            mockk(relaxed = true),
            mockk(relaxed = true),
            sharedPreferencesManager,
            testCoroutineRule.testCoroutineDispatcher
        )
        mockkStatic("com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManagerImplKt")
    }

    @Test
    fun createSession_shouldInvokeCreateSessionAndAddIntentParsingEventInCoreLib() {
        runBlocking {
            val session = mockk<SessionCaptureEvent>()
            every { session.id } returns UUID.randomUUID().toString()
            coEvery { coreEventEventsMgrMock.getCurrentCaptureSessionEvent() } returns session
            clientSessionEventsMgr.createSession(IntegrationInfo.ODK)

            coVerify { coreEventEventsMgrMock.createSession() }
            coVerify(exactly = 1) {
                coreEventEventsMgrMock.addOrUpdateEvent(match {
                    it is IntentParsingEvent && it.payload.integration == CoreIntegrationInfo.ODK
                })
            }
        }
    }

    @Test
    fun addAlertScreenEvent_shouldAddCoreLibEvent() {
        runBlocking {
            val clientApiAlert = ClientApiAlert.INVALID_PROJECT_ID
            clientSessionEventsMgr.addAlertScreenEvent(clientApiAlert)

            coVerify(exactly = 1) {
                coreEventEventsMgrMock.addOrUpdateEvent(match {
                    println("test ${it is AlertScreenEvent && it.payload.alertType == INVALID_PROJECT_ID}")
                    it is AlertScreenEvent && it.payload.alertType == INVALID_PROJECT_ID
                })
            }
        }
    }

    @Test
    fun addSuspiciousIntentEvent_shouldAddCoreLibEvent() {
        runBlocking {
            val unexpectedKey = mapOf("some_key" to "some_extra_value")
            clientSessionEventsMgr.addSuspiciousIntentEvent(unexpectedKey)

            coVerify(exactly = 1) {
                coreEventEventsMgrMock.addOrUpdateEvent(match {
                    it is SuspiciousIntentEvent && it.payload.unexpectedExtras == unexpectedKey
                })
            }
        }
    }

    @Test
    fun addInvalidIntentEvent_shouldAddCoreLibEvent() {
        runBlocking {
            val wrongKey = mapOf("some_wrong_key" to "some_wrong_value")
            val action = "action"
            clientSessionEventsMgr.addInvalidIntentEvent(action, wrongKey)

            coVerify(exactly = 1) {
                coreEventEventsMgrMock.addOrUpdateEvent(withArg {
                    assertThat(it).isInstanceOf(InvalidIntentEvent::class.java)
                    assertThat((it as InvalidIntentEvent).payload.action).isEqualTo(action)
                    assertThat(it.payload.extras).isEqualTo(wrongKey)
                })
            }
        }
    }

    @Test
    fun `get all events in session returns all data when can sync all data`(): Unit =
        runBlocking {
            every { sharedPreferencesManager.canCoSyncAllData() } returns true
            coEvery { clientSessionEventsMgr.getCurrentSessionId() } returns "sessionId"

            coEvery { coreEventEventsMgrMock.getEventsFromSession("sessionId") } returns allEventsFlow

            val events = clientSessionEventsMgr.getAllEventsForSession("sessionId").toList()
            assertThat(events.size).isEqualTo(6)
        }

    @Test
    fun `get all events in session returns correct data when can sync all only biometric`(): Unit =
        runBlocking {
            every { sharedPreferencesManager.canCoSyncBiometricData() } returns true
            coEvery { clientSessionEventsMgr.getCurrentSessionId() } returns "sessionId"

            coEvery { coreEventEventsMgrMock.getEventsFromSession("sessionId") } returns allEventsFlow

            val events = clientSessionEventsMgr.getAllEventsForSession("sessionId").toList()
            assertThat(events.size).isEqualTo(4)
            assertThat(events.any { it is EnrolmentEventV2 || it is PersonCreationEvent || it is FingerprintCaptureBiometricsEvent || it is FaceCaptureBiometricsEvent }).isTrue()
        }

    @Test
    fun `get all events in session returns correct data when can sync all only analytics`(): Unit =
        runBlocking {
            every { sharedPreferencesManager.canCoSyncAnalyticsData() } returns true
            coEvery { clientSessionEventsMgr.getCurrentSessionId() } returns "sessionId"

            coEvery { coreEventEventsMgrMock.getEventsFromSession("sessionId") } returns allEventsFlow

            val events = clientSessionEventsMgr.getAllEventsForSession("sessionId").toList()
            assertThat(events.size).isEqualTo(4)
            assertThat(events.any { it is FingerprintCaptureBiometricsEvent || it is FaceCaptureBiometricsEvent }).isFalse()
        }

    @Test
    fun `get all events in session returns empty when cannot sync any data`(): Unit =
        runBlocking {
            every { sharedPreferencesManager.canCoSyncAllData() } returns false
            every { sharedPreferencesManager.canCoSyncBiometricData() } returns false
            every { sharedPreferencesManager.canCoSyncAnalyticsData() } returns false
            coEvery { clientSessionEventsMgr.getCurrentSessionId() } returns "sessionId"

            coEvery { coreEventEventsMgrMock.getEventsFromSession("sessionId") } returns allEventsFlow

            val events = clientSessionEventsMgr.getAllEventsForSession("sessionId").toList()
            assertThat(events).isEmpty()
        }

    private val allEventsFlow = flowOf(
        EnrolmentEventV2(
            createdAt = 23,
            subjectId = "siD",
            projectId = "pId",
            moduleId = "mId",
            attendantId = "aiD",
            personCreationEventId = "pCId",
            labels = EventLabels()
        ),
        PersonCreationEvent(
            startTime = 34,
            fingerprintCaptureIds = emptyList(),
            fingerprintReferenceId = "fRId",
            faceCaptureIds = emptyList(),
            faceReferenceId = "someId",
            labels = EventLabels()
        ),
        FingerprintCaptureBiometricsEvent(
            id = "", labels = EventLabels(
                projectId = null,
                attendantId = null,
                moduleIds = listOf(),
                mode = listOf(),
                sessionId = null,
                deviceId = null
            ), payload = FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload(
                createdAt = 0,
                eventVersion = 0,
                fingerprint = FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Fingerprint(
                    finger = IFingerIdentifier.LEFT_3RD_FINGER,
                    template = "",
                    quality = 0
                ),
                id = "",
                endedAt = 0
            ), type = EventType.FINGERPRINT_CAPTURE_BIOMETRICS
        ),
        FaceCaptureBiometricsEvent(
            id = "", labels = EventLabels(
                projectId = null,
                attendantId = null,
                moduleIds = listOf(),
                mode = listOf(),
                sessionId = null,
                deviceId = null
            ), payload = FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload(
                id = "",
                createdAt = 0,
                eventVersion = 0,
                face = FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Face(
                    template = "",
                    quality = 0.0f
                ),
                endedAt = 0
            ), type = EventType.FACE_CAPTURE_BIOMETRICS
        ),
        ArtificialTerminationEvent(
            createdAt = 1,
            reason = Reason.TIMED_OUT,
            labels = EventLabels()
        ),
        AlertScreenEvent(createdAt = 1, alertType = NFC_NOT_ENABLED)
    )
}
