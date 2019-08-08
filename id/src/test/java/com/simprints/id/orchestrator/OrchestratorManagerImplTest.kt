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
import com.simprints.id.domain.moduleapi.face.FaceToDomainResponse.fromFaceToDomainResponse
import com.simprints.id.orchestrator.builders.AppResponseFactory
import com.simprints.id.orchestrator.modality.ModalityFlow
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Request
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.Step.Status.ONGOING
import com.simprints.id.orchestrator.steps.face.BaseFaceStepProcessor
import com.simprints.id.orchestrator.steps.face.BaseFaceStepProcessor.Companion.FACE_ENROL_REQUEST_CODE
import com.simprints.id.orchestrator.steps.fingerprint.BaseFingerprintStepProcessor
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.syntax.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
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

        intending(toPackage(BaseFingerprintStepProcessor.ACTIVITY_CLASS_NAME)).respondWith(ActivityResult(Activity.RESULT_OK, null))
        intending(toPackage(BaseFaceStepProcessor.ACTIVITY_CLASS_NAME)).respondWith(ActivityResult(Activity.RESULT_OK, null))
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
        verify(modalityFlowMock, times(nTimes)).getNextStepToStart()

    private fun verifyOrchestratorForwardedResultsToModalityFlow() =
        verifyOnce(modalityFlowMock) { handleIntentResult(anyInt(), anyInt(), anyNotNull()) }

    private fun verifyOrchestratorDidntTryToBuildFinalAppResponse() =
        verifyNever(appResponseFactoryMock) { buildAppResponse(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }

    private fun verifyOrchestratorTriedToBuildFinalAppResponse() =
        verifyOnce(appResponseFactoryMock) { buildAppResponse(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }

    private fun prepareModalFlowForFaceEnrol() {
        whenever(modalityFlowMock) { getNextStepToStart() } thenAnswer Answer { mockSteps.firstOrNull { it.status == NOT_STARTED } }
        mockSteps.add(Step(Request(FACE_ENROL_REQUEST_CODE, Intent()), NOT_STARTED))
    }

    private fun buildOrchestratorManager(): OrchestratorManager {
        val modalityFlowFactoryMock = mock<ModalityFlowFactory>().apply {
            whenever(this) { startModalityFlow(any(), any()) } thenReturn modalityFlowMock
        }

        return OrchestratorManagerImpl(modalityFlowFactoryMock, appResponseFactoryMock)
    }

    private suspend fun OrchestratorManager.startFlow(
        modalities: List<Modality>,
        request: AppRequest = appEnrolRequest,
        sessionId: String = "") = start(modalities, request, sessionId)

    private suspend fun OrchestratorManager.progressWitFaceEnrol(requestCode: Int = FACE_ENROL_REQUEST_CODE,
                                                                 response: FaceEnrolResponse? = FaceEnrolResponse(SOME_GUID)) {

        response?.let {
            mockSteps.firstOrNull { it.status == ONGOING }?.result = fromFaceToDomainResponse(response)
        }

        onModalStepRequestDone(
            requestCode,
            Activity.RESULT_OK,
            Intent().putExtra(FACE_BUNDLE_KEY, response))
    }

    companion object {
        private const val SOME_GUID = "some_guid"
        private const val WRONG_REQUEST_CODE = 1
    }
}
