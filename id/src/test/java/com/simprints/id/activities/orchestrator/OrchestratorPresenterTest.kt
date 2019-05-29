package com.simprints.id.activities.orchestrator

import android.content.Intent
import com.simprints.id.commontesttools.sessionEvents.createFakeSession
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.domain.moduleapi.app.responses.*
import com.simprints.id.orchestrator.OrchestratorManager
import com.simprints.id.orchestrator.modality.ModalityStepRequest
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.tools.TimeHelper
import com.simprints.testtools.common.syntax.*
import io.reactivex.Completable
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
            whenever(this) { startFlow(anyNotNull(), anyNotNull()) } thenReturn Observable.just(modalityRequest)
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
            whenever(this) { startFlow(anyNotNull(), anyNotNull()) } thenReturn Observable.error(Throwable("Error trying to generate the next ModalityRequest"))
        }

        orchestratorPresenter.start()

        verifyOnce(orchestratorPresenter.view) { setCancelResultAndFinish() }
    }

    @Test
    fun givenOrchestratorPresenter_anAppResponseIsReceived_presenterShouldReturnIt() {
        val mockAppResponse = createAppEnrolResponse()
        val orchestratorPresenter = createOrchestratorPresenter()
        orchestratorPresenter.sessionEventsManager = mockSessionEventsManagerToReturnASessionId()
        orchestratorPresenter.orchestratorManager = mock<OrchestratorManager>().apply {
            whenever(this) { getAppResponse() } thenReturn Single.just(mockAppResponse)
            whenever(this) { startFlow(anyNotNull(), anyNotNull()) } thenReturn Observable.never()
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
            whenever(this) { startFlow(anyNotNull(), anyNotNull()) } thenReturn Observable.never()
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
                whenever(this) { getAppResponse() } thenReturn Single.just(createAppEnrolResponse())
                whenever(this) { startFlow(anyNotNull(), anyNotNull()) } thenReturn Observable.never()
            }

            whenever(this) { buildEnrolmentCallbackEvent(anyNotNull()) } thenReturn mock()
        }

        orchestratorPresenter.start()

        verifyOnce(orchestratorPresenter) { addCallbackEventInSessions(anyNotNull()) }
        verifyOnce(orchestratorPresenter) { buildEnrolmentCallbackEvent(anyNotNull()) }
    }

    @Test
    fun givenOrchestratorPresenter_anAppEnrolResponseHappens_identifyCallbackEventShouldBeAdded() {
        val orchestratorPresenter = spy(createOrchestratorPresenter()).apply {
            this.timeHelper = mock<TimeHelper>().apply {
                whenever(this) { now() } thenReturn 10L
            }

            this.sessionEventsManager = mockSessionEventsManagerToReturnASessionId()

            this.orchestratorManager = mock<OrchestratorManager>().apply {
                whenever(this) { getAppResponse() } thenReturn Single.just(createMockFoAppResponse<AppIdentifyResponse>(AppResponseType.IDENTIFY))
                whenever(this) { startFlow(anyNotNull(), anyNotNull()) } thenReturn Observable.never()
            }

            whenever(this) { buildEnrolmentCallbackEvent(anyNotNull()) } thenReturn mock()
        }

        orchestratorPresenter.start()

        verifyOnce(orchestratorPresenter) { addCallbackEventInSessions(anyNotNull()) }
        verifyOnce(orchestratorPresenter) { buildIdentificationCallbackEvent(anyNotNull()) }
    }

    @Test
    fun givenOrchestratorPresenter_anAppEnrolResponseHappens_verifyCallbackEventShouldBeAdded() {
        val orchestratorPresenter = spy(createOrchestratorPresenter()).apply {
            this.timeHelper = mock<TimeHelper>().apply {
                whenever(this) { now() } thenReturn 10L
            }

            this.sessionEventsManager = mockSessionEventsManagerToReturnASessionId()

            this.orchestratorManager = mock<OrchestratorManager>().apply {
                whenever(this) { getAppResponse() } thenReturn Single.just(createMockFoAppResponse<AppVerifyResponse>(AppResponseType.VERIFY))
                whenever(this) { startFlow(anyNotNull(), anyNotNull()) } thenReturn Observable.never()
            }

            whenever(this) { buildEnrolmentCallbackEvent(anyNotNull()) } thenReturn mock()
        }

        orchestratorPresenter.start()

        verifyOnce(orchestratorPresenter) { addCallbackEventInSessions(anyNotNull()) }
        verifyOnce(orchestratorPresenter) { buildVerificationCallbackEvent(anyNotNull()) }
    }

    @Test
    fun givenOrchestratorPresenter_anAppEnrolResponseHappens_refusalCallbackEventShouldBeAdded() {
        val orchestratorPresenter = spy(createOrchestratorPresenter()).apply {
            this.timeHelper = mock<TimeHelper>().apply {
                whenever(this) { now() } thenReturn 10L
            }

            this.sessionEventsManager = mockSessionEventsManagerToReturnASessionId()

            this.orchestratorManager = mock<OrchestratorManager>().apply {
                whenever(this) { getAppResponse() } thenReturn Single.just(createMockFoAppResponse<AppRefusalFormResponse>(AppResponseType.REFUSAL))
                whenever(this) { startFlow(anyNotNull(), anyNotNull()) } thenReturn Observable.never()
            }

            whenever(this) { buildEnrolmentCallbackEvent(anyNotNull()) } thenReturn mock()
        }

        orchestratorPresenter.start()

        verifyOnce(orchestratorPresenter) { addCallbackEventInSessions(anyNotNull()) }
        verifyOnce(orchestratorPresenter) { buildRefusalCallbackEvent(anyNotNull()) }
    }

    private fun mockSessionEventsManagerToReturnASessionId() =
        mock<SessionEventsManager>().apply {
            val sessionMock = mock<SessionEvents>()
            whenever(sessionMock) { id } thenReturn ""
            whenever(this) { getCurrentSession() } thenReturn Single.just(sessionMock)
            whenever(this) { updateSession(anyNotNull()) } thenAnswer {
                val callback = it.arguments[0] as (SessionEvents) -> Unit
                callback(mock())
                Completable.complete()
            }
        }

    private inline fun <reified T: AppResponse> createMockFoAppResponse(type: AppResponseType): T =
        mock<T>().apply { whenever(this) { this.type } thenReturn type }

    private fun createAppEnrolResponse(): AppEnrolResponse =
        createMockFoAppResponse<AppEnrolResponse>(AppResponseType.ENROL).apply {
            whenever(this) { guid } thenReturn ""
        }


    private fun createOrchestratorPresenter() =
        OrchestratorPresenter().apply {
            syncSchedulerHelper = mock()
            appRequest = mock()
        }
}
