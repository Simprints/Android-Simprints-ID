package com.simprints.id.activities.orchestrator

import android.content.Intent
import com.nhaarman.mockito_kotlin.any
import com.simprints.id.commontesttools.sessionEvents.createFakeSession
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.domain.moduleapi.app.responses.*
import com.simprints.id.orchestrator.OrchestratorManager
import com.simprints.id.orchestrator.modality.ModalityStepRequest
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.tools.TimeHelper
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.spy
import com.simprints.testtools.common.syntax.verifyOnce
import com.simprints.testtools.common.syntax.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class OrchestratorPresenterTest {

    @Before
    fun setUp() {
        UnitTestConfig(this).rescheduleRxMainThread()
        mockOrchestratorDI()
    }

    @Test
    fun givenOrchestratorPresenter_startIsCalled_orchestratorShouldSubscribeForModalitiesRequests() {
        val orchestratorPresenter = spy(createOrchestratorPresenter()).apply {
            sessionEventsManager = mock<SessionEventsManager>().apply {
                whenever(this) { getCurrentSession() } thenReturn Single.just(createFakeSession())
            }
            whenever(this) { subscribeForModalitiesResponses() } thenReturn mock()
            whenever(this) { subscribeForFinalAppResponse() } thenReturn mock()
        }

        orchestratorPresenter.start()

        verifyOnce(orchestratorPresenter) { subscribeForModalitiesResponses() }
    }

    @Test
    fun givenOrchestratorPresenter_aModalityRequestIsReceived_presenterShouldLaunchAnIntent() {
        val modalityRequest = ModalityStepRequest(1, Intent())
        val orchestratorPresenter = createOrchestratorPresenter()
        orchestratorPresenter.sessionEventsManager = mockSessionEventsManagerToReturnASessionId()
        orchestratorPresenter.orchestratorManager = mock<OrchestratorManager>().apply {
            whenever(this) { getAppResponse() } thenReturn Single.never()
            whenever(this) { startFlow(any(), any()) } thenReturn Observable.just(modalityRequest)
        }

        orchestratorPresenter.start()

        verifyOnce(orchestratorPresenter.view) { startNextActivity(modalityRequest.requestCode, modalityRequest.intent) }
    }

    @Test
    fun givenOrchestratorPresenter_aModalityRequestErrorHappens_presenterShouldReturnAnError() {
        val orchestratorPresenter = createOrchestratorPresenter()
        orchestratorPresenter.sessionEventsManager = mockSessionEventsManagerToReturnASessionId()
        orchestratorPresenter.orchestratorManager = mock<OrchestratorManager>().apply {
            whenever(this) { getAppResponse() } thenReturn Single.never()
            whenever(this) { startFlow(any(), any()) } thenReturn Observable.error(Throwable("Error trying to generate the next ModalityRequest"))
        }

        orchestratorPresenter.start()

        verifyOnce(orchestratorPresenter.view) { setCancelResultAndFinish() }
    }

    @Test
    fun givenOrchestratorPresenter_anAppResponseIsReceived_presenterShouldReturnIt() {
        val mockAppResponse = mock<AppResponse>()
        val orchestratorPresenter = createOrchestratorPresenter()
        orchestratorPresenter.sessionEventsManager = mockSessionEventsManagerToReturnASessionId()
        orchestratorPresenter.orchestratorManager = mock<OrchestratorManager>().apply {
            whenever(this) { getAppResponse() } thenReturn Single.just(mockAppResponse)
            whenever(this) { startFlow(any(), any()) } thenReturn Observable.never()
        }

        orchestratorPresenter.start()

        verifyOnce(orchestratorPresenter.view) { setResultAndFinish(mockAppResponse) }
    }

    @Test
    fun givenOrchestratorPresenter_anAppResponseErrorHappens_presenterShouldReturnAnError() {
        val orchestratorPresenter = createOrchestratorPresenter()
        orchestratorPresenter.sessionEventsManager = mockSessionEventsManagerToReturnASessionId()
        orchestratorPresenter.orchestratorManager = mock<OrchestratorManager>().apply {
            whenever(this) { getAppResponse() } thenReturn Single.error(Throwable("Error trying to generate App Response"))
            whenever(this) { startFlow(any(), any()) } thenReturn Observable.never()
        }

        orchestratorPresenter.start()

        verifyOnce(orchestratorPresenter.view) { setCancelResultAndFinish() }
    }

    @Test
    fun givenOrchestratorPresenter_anAppEnrolResponseHappens_enrolmentCallbackEventShouldBeAdded() {
        val orchestratorPresenter = spy(createOrchestratorPresenter()).apply {
            this.timeHelper = mock<TimeHelper>().apply {
                whenever(this) { now() } thenReturn 10L
            }

            this.sessionEventsManager = mockSessionEventsManagerToReturnASessionId()

            this.orchestratorManager = mock<OrchestratorManager>().apply {
                whenever(this) { getAppResponse() } thenReturn Single.just(mock<AppEnrolResponse>())
                whenever(this) { startFlow(any(), any()) } thenReturn Observable.never()
            }

            whenever(this) { buildEnrolmentCallbackEvent(any()) } thenReturn mock()
        }

        orchestratorPresenter.start()

        verifyOnce(orchestratorPresenter) { addCallbackEventInSessions(any()) }
        verifyOnce(orchestratorPresenter) { buildEnrolmentCallbackEvent(any()) }
    }

    @Test
    fun givenOrchestratorPresenter_anAppEnrolResponseHappens_identifyCallbackEventShouldBeAdded() {
        val orchestratorPresenter = spy(createOrchestratorPresenter()).apply {
            this.timeHelper = mock<TimeHelper>().apply {
                whenever(this) { now() } thenReturn 10L
            }

            this.sessionEventsManager = mockSessionEventsManagerToReturnASessionId()

            this.orchestratorManager = mock<OrchestratorManager>().apply {
                whenever(this) { getAppResponse() } thenReturn Single.just(mock<AppIdentifyResponse>())
                whenever(this) { startFlow(any(), any()) } thenReturn Observable.never()
            }

            whenever(this) { buildEnrolmentCallbackEvent(any()) } thenReturn mock()
        }

        orchestratorPresenter.start()

        verifyOnce(orchestratorPresenter) { addCallbackEventInSessions(any()) }
        verifyOnce(orchestratorPresenter) { buildIdentificationCallbackEvent(any()) }
    }

    @Test
    fun givenOrchestratorPresenter_anAppEnrolResponseHappens_verifyCallbackEventShouldBeAdded() {
        val orchestratorPresenter = spy(createOrchestratorPresenter()).apply {
            this.timeHelper = mock<TimeHelper>().apply {
                whenever(this) { now() } thenReturn 10L
            }

            this.sessionEventsManager = mockSessionEventsManagerToReturnASessionId()

            this.orchestratorManager = mock<OrchestratorManager>().apply {
                whenever(this) { getAppResponse() } thenReturn Single.just(mock<AppVerifyResponse>())
                whenever(this) { startFlow(any(), any()) } thenReturn Observable.never()
            }

            whenever(this) { buildEnrolmentCallbackEvent(any()) } thenReturn mock()
        }

        orchestratorPresenter.start()

        verifyOnce(orchestratorPresenter) { addCallbackEventInSessions(any()) }
        verifyOnce(orchestratorPresenter) { buildVerificationCallbackEvent(any()) }
    }

    @Test
    fun givenOrchestratorPresenter_anAppEnrolResponseHappens_refusalCallbackEventShouldBeAdded() {
        val orchestratorPresenter = spy(createOrchestratorPresenter()).apply {
            this.timeHelper = mock<TimeHelper>().apply {
                whenever(this) { now() } thenReturn 10L
            }

            this.sessionEventsManager = mockSessionEventsManagerToReturnASessionId()

            this.orchestratorManager = mock<OrchestratorManager>().apply {
                whenever(this) { getAppResponse() } thenReturn Single.just(mock<AppRefusalFormResponse>())
                whenever(this) { startFlow(any(), any()) } thenReturn Observable.never()
            }

            whenever(this) { buildEnrolmentCallbackEvent(any()) } thenReturn mock()
        }

        orchestratorPresenter.start()

        verifyOnce(orchestratorPresenter) { addCallbackEventInSessions(any()) }
        verifyOnce(orchestratorPresenter) { buildRefusalCallbackEvent(any()) }
    }

    private fun mockSessionEventsManagerToReturnASessionId() =
        mock<SessionEventsManager>().apply {
            val sessionMock = mock<SessionEvents>()
            whenever(sessionMock) { id } thenReturn ""
            whenever(this) { getCurrentSession() } thenReturn Single.just(sessionMock)
        }

    private fun createOrchestratorPresenter() =
        OrchestratorPresenter().apply {
            syncSchedulerHelper = mock()
            appRequest = mock()
        }
}
