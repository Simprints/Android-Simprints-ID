package com.simprints.clientapi.controllers.core.eventData

import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.id.data.db.session.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.session.eventdata.models.domain.events.AlertScreenEvent
import com.simprints.id.data.db.session.eventdata.models.domain.events.IntentParsingEvent
import com.simprints.id.data.db.session.eventdata.models.domain.events.InvalidIntentEvent
import com.simprints.id.data.db.session.eventdata.models.domain.events.SuspiciousIntentEvent
import com.simprints.testtools.common.syntax.*
import com.simprints.testtools.unit.BaseUnitTestConfig
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import com.nhaarman.mockitokotlin2.any as MockitoArgThat
import com.nhaarman.mockitokotlin2.argThat as MockitoArgThat
import com.simprints.id.data.db.session.eventdata.models.domain.events.AlertScreenEvent.AlertScreenEventType
import com.simprints.id.data.db.session.eventdata.models.domain.session.SessionEvents
import com.simprints.id.data.db.session.eventdata.models.domain.events.IntentParsingEvent.IntegrationInfo as CoreIntegrationInfo

class ClientApiSessionEventsManagerImplTest {

    private lateinit var coreSessionEventsMgrMock: SessionEventsManager
    private lateinit var clientSessionEventsMgr: ClientApiSessionEventsManagerImpl

    @Before
    fun setup() {
        BaseUnitTestConfig().coroutinesMainThread().rescheduleRxMainThread()

        coreSessionEventsMgrMock = mockCoreSessionEventsManager()
        clientSessionEventsMgr = ClientApiSessionEventsManagerImpl(coreSessionEventsMgrMock, mock())
    }

    @Test
    fun createSession_shouldInvokeCreateSessionAndAddIntentParsingEventInCoreLib() {
        runBlocking {
            clientSessionEventsMgr.createSession(IntegrationInfo.ODK)

            verifyOnce(coreSessionEventsMgrMock) { createSession(any()) }
            verifyOnce(coreSessionEventsMgrMock) {
                addEvent(argThat {
                    Truth.assertThat(it).isInstanceOf(IntentParsingEvent::class.java)
                    Truth.assertThat((it as IntentParsingEvent).integration).isEqualTo(CoreIntegrationInfo.ODK)
                })
            }
        }
    }

    @Test
    fun addAlertScreenEvent_shouldAddCoreLibEvent() {
        runBlocking {
            val clientApiAlert = ClientApiAlert.INVALID_PROJECT_ID
            clientSessionEventsMgr.addAlertScreenEvent(clientApiAlert).blockingAwait()

            verifyOnce(coreSessionEventsMgrMock) {
                addEvent(argThat {
                    Truth.assertThat(it).isInstanceOf(AlertScreenEvent::class.java)
                    Truth.assertThat((it as AlertScreenEvent).alertType).isEqualTo(AlertScreenEventType.INVALID_PROJECT_ID)
                })
            }
        }
    }

    @Test
    fun addSuspiciousIntentEvent_shouldAddCoreLibEvent() {
        runBlocking {
            val unexpectedKey = mapOf("some_key" to "some_extra_value")
            clientSessionEventsMgr.addSuspiciousIntentEvent(unexpectedKey).blockingAwait()

            verifyOnce(coreSessionEventsMgrMock) {
                addEvent(argThat {
                    Truth.assertThat(it).isInstanceOf(SuspiciousIntentEvent::class.java)
                    Truth.assertThat((it as SuspiciousIntentEvent).unexpectedExtras).isEqualTo(unexpectedKey)
                })
            }
        }
    }

    @Test
    fun addInvalidIntentEvent_shouldAddCoreLibEvent() {
        runBlocking {
            val wrongKey = mapOf("some_wrong_key" to "some_wrong_value")
            val action = "action"
            clientSessionEventsMgr.addInvalidIntentEvent(action, wrongKey).blockingAwait()

            verifyOnce(coreSessionEventsMgrMock) {
                addEvent(argThat {
                    Truth.assertThat(it).isInstanceOf(InvalidIntentEvent::class.java)
                    Truth.assertThat((it as InvalidIntentEvent).action).isEqualTo(action)
                    Truth.assertThat(it.extras).isEqualTo(wrongKey)
                })
            }
        }
    }

    private fun mockCoreSessionEventsManager(): SessionEventsManager =
        mock<SessionEventsManager>().apply {
            val session = mock<SessionEvents>().apply {
                whenever(this) { id } thenReturn "session_id"
            }
            wheneverOnSuspend(this) { createSession(any()) } thenOnBlockingReturn (Single.just(session))
            whenever(this) { addEvent(any()) } thenReturn Completable.complete()
        }
}
