package com.simprints.id.orchestrator

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.face.ModuleApiToDomainFaceResponse.fromModuleApiToDomainFaceResponse
import com.simprints.id.orchestrator.responsebuilders.AppResponseFactory
import com.simprints.id.orchestrator.modality.ModalityFlow
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.Step.Status.ONGOING
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.*
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.testtools.common.syntax.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mockito.*
import org.mockito.stubbing.Answer
import com.simprints.moduleapi.face.responses.IFaceResponse.Companion.BUNDLE_KEY as FACE_BUNDLE_KEY

@RunWith(AndroidJUnit4::class)
class OrchestratorManagerImplTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private lateinit var appResponseFactoryMock: AppResponseFactory
    private lateinit var modalityFlowMock: ModalityFlow
    private lateinit var orchestrator: OrchestratorManager
    private val mockSteps = mutableListOf<Step>()
    val modalities = listOf(FACE)

    private val appEnrolRequest = AppEnrolRequest("some_project_id", "some_user_id", "some_module_id", "some_metadata")

    @Before
    fun setUp() {
        UnitTestConfig(this)
            .coroutinesMainThread()
            .rescheduleRxMainThread()

        Intents.init()

        mockSteps.clear()
        modalityFlowMock = mock<ModalityFlow>().apply {
            whenever(this) { this.steps } thenAnswer Answer { mockSteps }
        }
        appResponseFactoryMock = mock()

        orchestrator = buildOrchestratorManager()
        prepareModalFlowForFaceEnrol()

        intending(toPackage(FaceStepProcessorImpl.ACTIVITY_CLASS_NAME)).respondWith(ActivityResult(Activity.RESULT_OK, null))
    }

    @Test
    fun orchestratorStarts_shouldGetFirstStepFromModalityFlow() {
        with(orchestrator) {
            runBlocking {
                startFlow(modalities)
            }

            verifyOrchestratorGotNextStepFromModalityFlow()
        }
    }

    @Test
    fun modalityFlowCompletes_orchestratorShouldTryToBuildAppResponse() {
        with(orchestrator) {
            runBlocking {
                startFlow(modalities)
                progressWitFaceEnrol()
            }

            verifyOrchestratorTriedToBuildFinalAppResponse()
        }
    }

    @Test
    fun modalityFlowReceivesAWrongResult_orchestratorShouldNotGoAhead() {
        with(orchestrator) {
            runBlocking {
                startFlow(modalities)
                progressWitFaceEnrol(WRONG_REQUEST_CODE, null)
            }

            verifyOrchestratorDidntTryToBuildFinalAppResponse()
        }
    }

    @Test
    fun orchestratorReceivesAResult_itShouldBeForwardedToModalityFlowAndMoveOn() {
        with(orchestrator) {
            runBlocking {
                startFlow(modalities)
                progressWitFaceEnrol()
            }

            verifyOrchestratorForwardedResultsToModalityFlow()
            verifyOrchestratorGotNextStepFromModalityFlow(2)
        }
    }

    private fun verifyOrchestratorGotNextStepFromModalityFlow(nTimes: Int = 1) =
        verify(modalityFlowMock, times(nTimes)).getNextStepToLaunch()

    private fun verifyOrchestratorForwardedResultsToModalityFlow() =
        verifyOnce(modalityFlowMock) { handleIntentResult(anyInt(), anyInt(), anyNotNull()) }

    private fun verifyOrchestratorDidntTryToBuildFinalAppResponse() =
        verifyNever(appResponseFactoryMock) { buildAppResponse(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }

    private fun verifyOrchestratorTriedToBuildFinalAppResponse() =
        verifyOnce(appResponseFactoryMock) { buildAppResponse(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }

    private fun prepareModalFlowForFaceEnrol() {
        whenever(modalityFlowMock) { getNextStepToLaunch() } thenAnswer Answer { mockSteps.firstOrNull { it.status == NOT_STARTED } }
        mockSteps.add(Step(ENROL.value, FaceStepProcessorImpl.ACTIVITY_CLASS_NAME, IFaceRequest.BUNDLE_KEY, mock(), NOT_STARTED))
    }

    private fun buildOrchestratorManager(): OrchestratorManager {
        val modalityFlowFactoryMock = mock<ModalityFlowFactory>().apply {
            whenever(this) { createModalityFlow(any(), any()) } thenReturn modalityFlowMock
        }

        return OrchestratorManagerImpl(modalityFlowFactoryMock, appResponseFactoryMock)
    }

    private suspend fun OrchestratorManager.startFlow(
        modalities: List<Modality>,
        request: AppRequest = appEnrolRequest,
        sessionId: String = "") = initialise(modalities, request, sessionId)

    private suspend fun OrchestratorManager.progressWitFaceEnrol(requestCode: Int = ENROL.value,
                                                                 response: FaceEnrolResponse? = FaceEnrolResponse(SOME_GUID)) {

        response?.let {
            mockSteps.firstOrNull { it.status == ONGOING }?.result = fromModuleApiToDomainFaceResponse(response)
        }

        handleIntentResult(
            requestCode,
            Activity.RESULT_OK,
            Intent().putExtra(FACE_BUNDLE_KEY, response))
    }

    companion object {
        private const val SOME_GUID = "some_guid"
        private const val WRONG_REQUEST_CODE = 1
    }

    @After
    fun tearDown(){
        Intents.release()
        stopKoin()
    }
}
