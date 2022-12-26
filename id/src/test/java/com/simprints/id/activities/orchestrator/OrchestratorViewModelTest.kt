package com.simprints.id.activities.orchestrator

import android.app.Activity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_METADATA
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.eventsystem.sampledata.createSessionCaptureEvent
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponseType
import com.simprints.id.domain.moduleapi.app.responses.AppResponseType.ENROL
import com.simprints.id.orchestrator.OrchestratorManager
import com.simprints.id.orchestrator.steps.Step
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OrchestratorViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var eventRepositoryMock: EventRepository

    @MockK
    private lateinit var orchestratorEventsHelperMock: OrchestratorEventsHelper

    @MockK
    private lateinit var orchestratorManagerMock: OrchestratorManager

    @MockK
    private lateinit var domainToModuleApiConverter: DomainToModuleApiAppResponse

    @MockK
    private lateinit var configManager: ConfigManager

    private lateinit var liveDataAppResponse: MutableLiveData<AppResponse>
    private lateinit var liveDataNextIntent: MutableLiveData<Step>

    private val enrolAppRequest =
        AppEnrolRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA)
    private val fakeSession = createSessionCaptureEvent()

    private lateinit var vm: OrchestratorViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        liveDataAppResponse = MutableLiveData()
        liveDataNextIntent = MutableLiveData()

        configureMocks()

        vm = OrchestratorViewModel(
            orchestratorManagerMock,
            orchestratorEventsHelperMock,
            configManager,
            eventRepositoryMock,
            domainToModuleApiConverter,
            testCoroutineRule.testCoroutineDispatcher
        )
    }

    private fun configureMocks() {
        every { domainToModuleApiConverter.fromDomainModuleApiAppResponse(any()) } returns mockk()
        coEvery { eventRepositoryMock.getCurrentCaptureSessionEvent() } returns fakeSession
        every { orchestratorManagerMock.appResponse } returns liveDataAppResponse
        every { orchestratorManagerMock.ongoingStep } returns liveDataNextIntent
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general } returns mockk {
                every { modalities } returns listOf(GeneralConfiguration.Modality.FACE)
            }
        }
    }

    @Test
     fun `test viewModel Start with restoreState=false should InitializeOrchestrator and start modality flow`() = runTest {
        vm.startOrRestoreModalityFlow(enrolAppRequest, false)
        coVerify {
            orchestratorManagerMock.initialise(
                listOf(GeneralConfiguration.Modality.FACE),
                enrolAppRequest,
                fakeSession.id
            )
        }
        coVerify { orchestratorManagerMock.startModalityFlow() }
    }


    @Test
    fun `test viewModel Start with restoreState=true should InitializeOrchestrator and RestoreState`() = runTest {
        vm.startOrRestoreModalityFlow(enrolAppRequest, true)
        coVerify {
            orchestratorManagerMock.initialise(
                listOf(GeneralConfiguration.Modality.FACE),
                enrolAppRequest,
                fakeSession.id
            )
        }
        coVerify { orchestratorManagerMock.restoreState() }
    }


    @Test
    fun viewModelStart_shouldForwardResultToOrchestrator() = runTest {
        vm.startOrRestoreModalityFlow(enrolAppRequest,false)
        vm.onModalStepRequestDone(enrolAppRequest, REQUEST_CODE, Activity.RESULT_OK, null)
        coVerify(exactly = 1) {
            orchestratorManagerMock.handleIntentResult(
                enrolAppRequest,
                REQUEST_CODE,
                Activity.RESULT_OK,
                null
            )
        }
    }
    @Test
    fun viewModel_saveState_should_CallOrchestratorManager_saveState() = runTest {
        vm.startOrRestoreModalityFlow(enrolAppRequest,false)
        vm.saveState()
        coVerify {
            orchestratorManagerMock.saveState()
        }
    }


    @Test
    fun orchestratorCreatesAppResponse_viewModelShouldAddACallbackEvent() = runTest {
        vm.appResponse.observeForever {}

        liveDataAppResponse.postFakeAppResponse<AppEnrolResponse>(ENROL)

        every { orchestratorEventsHelperMock.addCallbackEventInSessions(any()) } just runs
        verify(exactly = 1) { orchestratorEventsHelperMock.addCallbackEventInSessions(any<AppEnrolResponse>()) }
    }


    private inline fun <reified T : AppResponse> MutableLiveData<AppResponse>.postFakeAppResponse(
        typeToReturn: AppResponseType
    ) {
        val appResponse = mockk<T>().apply {
            every { this@apply.type } returns typeToReturn
        }

        postValue(appResponse)
    }

    companion object {
        const val REQUEST_CODE = 0
    }
}
