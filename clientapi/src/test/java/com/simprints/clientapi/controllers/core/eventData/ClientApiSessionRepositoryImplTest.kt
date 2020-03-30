package com.simprints.clientapi.controllers.core.eventData

import com.google.common.truth.Truth
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.events.AlertScreenEvent
import com.simprints.id.data.db.session.domain.models.events.AlertScreenEvent.AlertScreenEventType
import com.simprints.id.data.db.session.domain.models.events.IntentParsingEvent
import com.simprints.id.data.db.session.domain.models.events.InvalidIntentEvent
import com.simprints.id.data.db.session.domain.models.events.SuspiciousIntentEvent
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.testtools.unit.BaseUnitTestConfig
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import java.util.*
import com.simprints.id.data.db.session.domain.models.events.IntentParsingEvent.IntegrationInfo as CoreIntegrationInfo

class ClientApiSessionRepositoryImplTest {

    private lateinit var coreSessionEventsMgrMock: SessionRepository
    private lateinit var clientSessionEventsMgr: ClientApiSessionEventsManagerImpl

    @Before
    fun setup() {
        BaseUnitTestConfig().coroutinesMainThread().rescheduleRxMainThread()

        coreSessionEventsMgrMock = mockk(relaxed = true)
        clientSessionEventsMgr = ClientApiSessionEventsManagerImpl(coreSessionEventsMgrMock, mockk(relaxed = true))
    }

    @Test
    fun createSession_shouldInvokeCreateSessionAndAddIntentParsingEventInCoreLib() {
        runBlockingTest {
            val session = mockk<SessionEvents>()
            every { session.id } returns UUID.randomUUID().toString()
            coEvery { coreSessionEventsMgrMock.getCurrentSession() } returns session
            clientSessionEventsMgr.createSession(IntegrationInfo.ODK)

            coVerify { coreSessionEventsMgrMock.createSession(any()) }
            coVerify(exactly = 1) {
                coreSessionEventsMgrMock.addEventToCurrentSessionInBackground(withArg {
                    Truth.assertThat(it).isInstanceOf(IntentParsingEvent::class.java)
                    Truth.assertThat((it as IntentParsingEvent).integration).isEqualTo(CoreIntegrationInfo.ODK)
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
                coreSessionEventsMgrMock.addEventToCurrentSessionInBackground(withArg {
                    Truth.assertThat(it).isInstanceOf(AlertScreenEvent::class.java)
                    Truth.assertThat((it as AlertScreenEvent).alertType).isEqualTo(AlertScreenEventType.INVALID_PROJECT_ID)
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
                coreSessionEventsMgrMock.addEventToCurrentSessionInBackground(withArg {
                    Truth.assertThat(it).isInstanceOf(SuspiciousIntentEvent::class.java)
                    Truth.assertThat((it as SuspiciousIntentEvent).unexpectedExtras).isEqualTo(unexpectedKey)
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
                coreSessionEventsMgrMock.addEventToCurrentSessionInBackground(withArg {
                    Truth.assertThat(it).isInstanceOf(InvalidIntentEvent::class.java)
                    Truth.assertThat((it as InvalidIntentEvent).action).isEqualTo(action)
                    Truth.assertThat(it.extras).isEqualTo(wrongKey)
                })
            }
        }
    }
}
