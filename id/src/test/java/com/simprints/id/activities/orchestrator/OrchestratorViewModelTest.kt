package com.simprints.id.activities.orchestrator

import android.app.Activity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.any
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.sessionEvents.createFakeSession
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponseType
import com.simprints.id.domain.moduleapi.app.responses.AppResponseType.ENROL
import com.simprints.id.orchestrator.OrchestratorManager
import com.simprints.id.orchestrator.SOME_METADATA
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.verifyOnce
import com.simprints.testtools.common.syntax.whenever
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class OrchestratorViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock private lateinit var sessionEventsManagerMock: SessionEventsManager
    @Mock private lateinit var orchestratorEventsHelperMock: OrchestratorEventsHelper
    @Mock private lateinit var orchestratorManagerMock: OrchestratorManager
    @Mock private lateinit var domainToModuleApiConverter: DomainToModuleApiAppResponse
    private lateinit var liveDataAppResponse: MutableLiveData<AppResponse>
    private lateinit var liveDataNextIntent: MutableLiveData<Step>

    private val enrolAppRequest = AppEnrolRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, SOME_METADATA)
    private val fakeSession = createFakeSession(id = SOME_SESSION_ID)

    private lateinit var vm: OrchestratorViewModel

    @Before
    fun setUp() {
        UnitTestConfig(this)
            .rescheduleRxMainThread()
            .coroutinesMainThread()

        MockitoAnnotations.initMocks(this)

        liveDataAppResponse = MutableLiveData()
        liveDataNextIntent = MutableLiveData()

        configureMocks()

        vm = OrchestratorViewModel(orchestratorManagerMock, orchestratorEventsHelperMock, listOf(FACE), sessionEventsManagerMock, domainToModuleApiConverter)
    }

    private fun configureMocks() {
        whenever(domainToModuleApiConverter) { fromDomainModuleApiAppResponse(anyNotNull()) } thenReturn mock()
        whenever(sessionEventsManagerMock) { getCurrentSession() } thenReturn Single.just(fakeSession)
        whenever(orchestratorManagerMock) { appResponse } thenReturn liveDataAppResponse
        whenever(orchestratorManagerMock) { ongoingStep } thenReturn liveDataNextIntent
    }

    @Test
    fun viewModelStart_shouldStartOrchestrator() {
        runBlocking {
            vm.startModalityFlow(enrolAppRequest)
            verifyOnce(orchestratorManagerMock) {
                runBlocking {
                    initialise(listOf(FACE), enrolAppRequest, SOME_SESSION_ID)
                }
            }
        }
    }

    @Test
    fun viewModelStart_shouldForwardResultToOrchestrator() {
        runBlocking {
            vm.onModalStepRequestDone(enrolAppRequest, REQUEST_CODE, Activity.RESULT_OK, null)
            verifyOnce(orchestratorManagerMock) {
                runBlocking {
                    handleIntentResult(enrolAppRequest, REQUEST_CODE, Activity.RESULT_OK, null)
                }
            }
        }
    }

    @Test
    fun orchestratorCreatesAppResponse_viewModelShouldAddACallbackEvent() {
        runBlocking {
            vm.appResponse.observeForever {}

            liveDataAppResponse.postFakeAppResponse<AppEnrolResponse>(ENROL)

            whenever(orchestratorEventsHelperMock) { addCallbackEventInSessions(anyNotNull()) } thenDoNothing {}
            verifyOnce(orchestratorEventsHelperMock) { addCallbackEventInSessions(any<AppEnrolResponse>()) }
        }
    }

    private inline fun <reified T : AppResponse> MutableLiveData<AppResponse>.postFakeAppResponse(typeToReturn: AppResponseType) {
        val appResponse = mock<T>().apply {
            whenever(this) { type } thenReturn typeToReturn
        }

        postValue(appResponse)
    }

    companion object {
        const val SOME_SESSION_ID = "some_session_id"
        const val REQUEST_CODE = 0
    }
}
