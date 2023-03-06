package com.simprints.clientapi.controllers.core.eventData

import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration.UpSynchronizationKind.*
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.*
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.INVALID_PROJECT_ID
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.NFC_NOT_ENABLED
import com.simprints.infra.events.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason
import com.simprints.infra.events.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.session.SessionCaptureEvent
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*
import com.simprints.infra.events.event.domain.models.IntentParsingEvent.IntentParsingPayload.IntegrationInfo as CoreIntegrationInfo

class ClientApiEventRepositoryImplTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK(relaxed = true)
    lateinit var coreEventEventsMgrMock: EventRepository

    private val coSyncConfiguration = mockk<CoSyncUpSynchronizationConfiguration>()
    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { synchronization } returns mockk {
                every { up } returns mockk {
                    every { coSync } returns coSyncConfiguration
                }
            }
        }
    }
    private lateinit var clientSessionEventsMgr: ClientApiSessionEventsManager

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        val scope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher)
        clientSessionEventsMgr = ClientApiSessionEventsManagerImpl(
            coreEventEventsMgrMock,
            mockk(relaxed = true),
            mockk(relaxed = true),
            configManager,
            scope
        )
    }

    @Test
    fun createSession_shouldInvokeCreateSessionAndAddIntentParsingEventInCoreLib() {
        runTest {
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
        runTest {
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
        runTest {
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
        runTest {
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
        runTest {
            every { coSyncConfiguration.kind } returns ALL
            coEvery { clientSessionEventsMgr.getCurrentSessionId() } returns "sessionId"

            coEvery { coreEventEventsMgrMock.getEventsFromSession("sessionId") } returns allEventsFlow

            val events = clientSessionEventsMgr.getAllEventsForSession("sessionId").toList()
            assertThat(events.size).isEqualTo(6)
        }

    @Test
    fun `get all events in session returns correct data when can sync all only biometric`(): Unit =
        runTest {
            every { coSyncConfiguration.kind } returns ONLY_BIOMETRICS
            coEvery { clientSessionEventsMgr.getCurrentSessionId() } returns "sessionId"

            coEvery { coreEventEventsMgrMock.getEventsFromSession("sessionId") } returns allEventsFlow

            val events = clientSessionEventsMgr.getAllEventsForSession("sessionId").toList()
            assertThat(events.size).isEqualTo(4)
            assertThat(events.any { it is EnrolmentEventV2 || it is PersonCreationEvent || it is FingerprintCaptureBiometricsEvent || it is FaceCaptureBiometricsEvent }).isTrue()
        }

    @Test
    fun `get all events in session returns correct data when can sync all only analytics`(): Unit =
        runTest {
            every { coSyncConfiguration.kind } returns ONLY_ANALYTICS
            coEvery { clientSessionEventsMgr.getCurrentSessionId() } returns "sessionId"

            coEvery { coreEventEventsMgrMock.getEventsFromSession("sessionId") } returns allEventsFlow

            val events = clientSessionEventsMgr.getAllEventsForSession("sessionId").toList()
            assertThat(events.size).isEqualTo(4)
            assertThat(events.any { it is FingerprintCaptureBiometricsEvent || it is FaceCaptureBiometricsEvent }).isFalse()
        }

    @Test
    fun `get all events in session returns empty when cannot sync any data`(): Unit =
        runTest {
            every { coSyncConfiguration.kind } returns NONE
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
                sessionId = null,
                deviceId = null
            ), payload = FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload(
                id = "",
                createdAt = 0,
                eventVersion = 0,
                face = FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Face(
                    roll = 0.0f,
                    yaw = 0.0f,
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
