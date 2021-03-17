package com.simprints.clientapi.controllers.core.eventData

import com.google.common.truth.Truth
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.AlertScreenEvent
import com.simprints.id.data.db.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import com.simprints.id.data.db.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.INVALID_PROJECT_ID
import com.simprints.id.data.db.event.domain.models.IntentParsingEvent
import com.simprints.id.data.db.event.domain.models.InvalidIntentEvent
import com.simprints.id.data.db.event.domain.models.SuspiciousIntentEvent
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*
import com.simprints.id.data.db.event.domain.models.IntentParsingEvent.IntentParsingPayload.IntegrationInfo as CoreIntegrationInfo

class ClientApiEventRepositoryImplTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK(relaxed = true) lateinit var coreEventEventsMgrMock: EventRepository
    private lateinit var clientSessionEventsMgr: ClientApiSessionEventsManager

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        clientSessionEventsMgr = ClientApiSessionEventsManagerImpl(coreEventEventsMgrMock, mockk(relaxed = true), testCoroutineRule.testCoroutineDispatcher)
    }

    @Test
    fun createSession_shouldInvokeCreateSessionAndAddIntentParsingEventInCoreLib() {
        runBlockingTest {
            val session = mockk<SessionCaptureEvent>()
            every { session.id } returns UUID.randomUUID().toString()
            coEvery { coreEventEventsMgrMock.getCurrentCaptureSessionEvent() } returns session
            clientSessionEventsMgr.createSession(IntegrationInfo.ODK)

            coVerify { coreEventEventsMgrMock.createSession() }
            coVerify(exactly = 1) {
                coreEventEventsMgrMock.addEventToCurrentSession(match {
                    it is IntentParsingEvent && it.payload.integration == CoreIntegrationInfo.ODK
                })
            }
        }
    }

    @Test
    fun addAlertScreenEvent_shouldAddCoreLibEvent() {
        runBlockingTest {
            val clientApiAlert = ClientApiAlert.INVALID_PROJECT_ID
            clientSessionEventsMgr.addAlertScreenEvent(clientApiAlert)

            coVerify(exactly = 1) {
                coreEventEventsMgrMock.addEventToCurrentSession(match {
                    println("test ${it is AlertScreenEvent && it.payload.alertType == INVALID_PROJECT_ID}")
                    it is AlertScreenEvent && it.payload.alertType == AlertScreenEventType.INVALID_PROJECT_ID
                })
            }
        }
    }

    @Test
    fun addSuspiciousIntentEvent_shouldAddCoreLibEvent() {
        runBlockingTest {
            val unexpectedKey = mapOf("some_key" to "some_extra_value")
            clientSessionEventsMgr.addSuspiciousIntentEvent(unexpectedKey)

            coVerify(exactly = 1) {
                coreEventEventsMgrMock.addEventToCurrentSession(match {
                    it is SuspiciousIntentEvent && it.payload.unexpectedExtras == unexpectedKey
                })
            }
        }
    }

    @Test
    fun addInvalidIntentEvent_shouldAddCoreLibEvent() {
        runBlockingTest {
            val wrongKey = mapOf("some_wrong_key" to "some_wrong_value")
            val action = "action"
            clientSessionEventsMgr.addInvalidIntentEvent(action, wrongKey)

            coVerify(exactly = 1) {
                coreEventEventsMgrMock.addEventToCurrentSession(withArg {
                    Truth.assertThat(it).isInstanceOf(InvalidIntentEvent::class.java)
                    Truth.assertThat((it as InvalidIntentEvent).payload.action).isEqualTo(action)
                    Truth.assertThat(it.payload.extras).isEqualTo(wrongKey)
                })
            }
        }
    }
}
