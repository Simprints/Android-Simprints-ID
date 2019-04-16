package com.simprints.id.activities.orchestrator

import android.content.Intent
import com.nhaarman.mockito_kotlin.any
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.OrchestratorManager
import com.simprints.id.orchestrator.modality.ModalityStepRequest
import com.simprints.id.testtools.UnitTestConfig
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
    }

    @Test
    fun givenOrchestratorPresenter_startIsCalled_orchestratorShouldSubscribeForModalitiesRequests() {
        val orchestratorPresenter = spy(OrchestratorPresenter(mock(), mock(), mock())).apply {
            whenever(this) { subscribeForModalitiesRequests() } thenReturn mock()
            whenever(this) { subscribeForFinalAppResponse() } thenReturn mock()
        }

        orchestratorPresenter.start()

        verifyOnce(orchestratorPresenter) { subscribeForModalitiesRequests() }
    }

    @Test
    fun givenOrchestratorPresenter_startIsCalled_itShouldSubscribeForAppResponse() {
        val orchestratorPresenter = spy(OrchestratorPresenter(mock(), mock(), mock())).apply {
            whenever(this) { subscribeForModalitiesRequests() } thenReturn mock()
            whenever(this) { subscribeForFinalAppResponse() } thenReturn mock()
        }

        orchestratorPresenter.start()

        verifyOnce(orchestratorPresenter) { subscribeForFinalAppResponse() }
    }

    @Test
    fun givenOrchestratorPresenter_aModalityRequestIsReceived_presenterShouldLaunchAnIntent() {
        val modalityRequest = ModalityStepRequest(1, Intent())
        val orchestratorPresenter = OrchestratorPresenter(mock(), mock(), mock())
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
        val orchestratorPresenter = OrchestratorPresenter(mock(), mock(), mock())
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
        val orchestratorPresenter = OrchestratorPresenter(mock(), mock(), mock())
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
        val orchestratorPresenter = OrchestratorPresenter(mock(), mock(), mock())
        orchestratorPresenter.sessionEventsManager = mockSessionEventsManagerToReturnASessionId()
        orchestratorPresenter.orchestratorManager = mock<OrchestratorManager>().apply {
            whenever(this) { getAppResponse() } thenReturn Single.error(Throwable("Error trying to generate App Response"))
            whenever(this) { startFlow(any(), any()) } thenReturn Observable.never()
        }

        orchestratorPresenter.start()

        verifyOnce(orchestratorPresenter.view) { setCancelResultAndFinish() }
    }

    private fun mockSessionEventsManagerToReturnASessionId() =
        mock<SessionEventsManager>().apply {
            val sessionMock = mock<SessionEvents>()
            whenever(sessionMock) { id } thenReturn ""
            whenever(this) { getCurrentSession() } thenReturn Single.just(sessionMock)
        }
}
