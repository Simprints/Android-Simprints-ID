package com.simprints.id.activities.orchestrator

import android.app.Activity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.sessionEvents.createFakeSession
import com.simprints.id.data.db.session.eventdata.controllers.domain.SessionEventsManager
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
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OrchestratorViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @MockK private lateinit var sessionEventsManagerMock: SessionEventsManager
    @MockK private lateinit var orchestratorEventsHelperMock: OrchestratorEventsHelper
    @MockK private lateinit var orchestratorManagerMock: OrchestratorManager
    @MockK private lateinit var domainToModuleApiConverter: DomainToModuleApiAppResponse
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

        MockKAnnotations.init(this, relaxed = true)

        liveDataAppResponse = MutableLiveData()
        liveDataNextIntent = MutableLiveData()

        configureMocks()

        vm = OrchestratorViewModel(orchestratorManagerMock, orchestratorEventsHelperMock, listOf(FACE), sessionEventsManagerMock, domainToModuleApiConverter)
    }

    private fun configureMocks() {
        every { domainToModuleApiConverter.fromDomainModuleApiAppResponse(any()) } returns mockk()
        every { sessionEventsManagerMock.getCurrentSession() } returns Single.just(fakeSession)
        every { orchestratorManagerMock.appResponse } returns liveDataAppResponse
        every { orchestratorManagerMock.ongoingStep } returns liveDataNextIntent
    }

    @Test
    fun viewModelStart_shouldStartOrchestrator() {
        runBlocking {
            vm.startModalityFlow(enrolAppRequest)
            coVerify(exactly = 1) { orchestratorManagerMock.initialise(listOf(FACE), enrolAppRequest, SOME_SESSION_ID) }
        }
    }

    @Test
    fun viewModelStart_shouldForwardResultToOrchestrator() {
        runBlocking {
            vm.onModalStepRequestDone(enrolAppRequest, REQUEST_CODE, Activity.RESULT_OK, null)
            coVerify(exactly = 1) { orchestratorManagerMock.handleIntentResult(enrolAppRequest, REQUEST_CODE, Activity.RESULT_OK, null) }
        }
    }

    @Test
    fun orchestratorCreatesAppResponse_viewModelShouldAddACallbackEvent() {
        runBlocking {
            vm.appResponse.observeForever {}

            liveDataAppResponse.postFakeAppResponse<AppEnrolResponse>(ENROL)

            every { orchestratorEventsHelperMock.addCallbackEventInSessions(any()) } just runs
            verify(exactly = 1) { orchestratorEventsHelperMock.addCallbackEventInSessions(any<AppEnrolResponse>()) }
        }
    }


    private inline fun <reified T : AppResponse> MutableLiveData<AppResponse>.postFakeAppResponse(typeToReturn: AppResponseType) {
        val appResponse = mockk<T>().apply {
            every { this@apply.type } returns typeToReturn
        }

        postValue(appResponse)
    }

    companion object {
        const val SOME_SESSION_ID = "some_session_id"
        const val REQUEST_CODE = 0
    }
}
