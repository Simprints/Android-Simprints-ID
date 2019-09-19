package com.simprints.id.orchestrator

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.responses.fromModuleApiToDomain
import com.simprints.id.orchestrator.cache.HotCacheImpl
import com.simprints.id.orchestrator.cache.crypto.StepEncoder
import com.simprints.id.orchestrator.modality.ModalityFlow
import com.simprints.id.orchestrator.responsebuilders.AppResponseFactory
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.Step.Status.ONGOING
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.CAPTURE
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceCaptureResponse
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
    private val modalities = listOf(FACE)

    private val appEnrolRequest = AppEnrolRequest(
        "some_project_id",
        "some_user_id",
        "some_module_id",
        "some_metadata"
    )

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

        intending(toPackage(FaceStepProcessorImpl.ACTIVITY_CLASS_NAME))
            .respondWith(ActivityResult(Activity.RESULT_OK, null))
    }

    @Test
    fun orchestratorStarts_shouldGetFirstStepFromModalityFlow() {
        runBlocking {
            orchestrator.startFlowForEnrol(modalities)
        }

        verifyOrchestratorGotNextStepFromModalityFlow()
    }

    @Test
    fun modalityFlowCompletes_orchestratorShouldTryToBuildAppResponse() {
        runBlocking {
            with(orchestrator) {
                startFlowForEnrol(modalities)
                progressWitFaceCapture()
            }
        }

        verifyOrchestratorTriedToBuildFinalAppResponse()
    }

    @Test
    fun modalityFlowReceivesAWrongResult_orchestratorShouldNotGoAhead() {
        runBlocking {
            with(orchestrator) {
                startFlowForEnrol(modalities)
                progressWitFaceCapture(WRONG_REQUEST_CODE, null)
            }

            verifyOrchestratorDidntTryToBuildFinalAppResponse()
        }
    }

    @Test
    fun orchestratorReceivesAResult_itShouldBeForwardedToModalityFlowAndMoveOn() {
        with(orchestrator) {
            runBlocking {
                startFlowForEnrol(modalities)
                progressWitFaceCapture()
            }

            verifyOrchestratorForwardedResultsToModalityFlow()
            verifyOrchestratorGotNextStepFromModalityFlow(2)
        }
    }

    @After
    fun tearDown() {
        Intents.release()
        stopKoin()
    }

    private fun verifyOrchestratorGotNextStepFromModalityFlow(nTimes: Int = 1) =
        verify(modalityFlowMock, times(nTimes)).getNextStepToLaunch()

    private fun verifyOrchestratorForwardedResultsToModalityFlow() =
        verifyOnce(modalityFlowMock) { handleIntentResult(anyInt(), anyInt(), anyNotNull()) }

    private fun verifyOrchestratorDidntTryToBuildFinalAppResponse() =
        verifyNever(appResponseFactoryMock) {
            buildAppResponse(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull())
        }

    private fun verifyOrchestratorTriedToBuildFinalAppResponse() =
        verifyOnce(appResponseFactoryMock) {
            buildAppResponse(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull())
        }

    private fun prepareModalFlowForFaceEnrol() {
        whenever(modalityFlowMock) {
            getNextStepToLaunch()
        } thenAnswer {
            mockSteps.firstOrNull { it.getStatus() == NOT_STARTED }
        }

        val nFaceSamplesToCapture = 3
        val request = FaceCaptureRequest(nFaceSamplesToCapture)

        mockSteps.add(
            Step(
                requestCode = CAPTURE.value,
                activityName = FaceStepProcessorImpl.ACTIVITY_CLASS_NAME,
                bundleKey = IFaceRequest.BUNDLE_KEY,
                request = request,
                status = NOT_STARTED
            )
        )
    }

    private fun buildOrchestratorManager(): OrchestratorManager {
        val modalityFlowFactoryMock = mock<ModalityFlowFactory>().apply {
            whenever(this) { createModalityFlow(any(), any()) } thenReturn modalityFlowMock
        }
        val preferences = mock<SharedPreferences>()
        val stepEncoder = mock<StepEncoder>()
        val hotCache = HotCacheImpl(preferences, stepEncoder)

        return OrchestratorManagerImpl(modalityFlowFactoryMock, appResponseFactoryMock, hotCache)
    }

    private fun OrchestratorManager.startFlowForEnrol(
        modalities: List<Modality>,
        sessionId: String = "") = initialise(modalities, appEnrolRequest, sessionId)

    private fun OrchestratorManager.progressWitFaceCapture(
        requestCode: Int = CAPTURE.value,
        response: IFaceCaptureResponse? = IFaceCaptureResponseImpl(emptyList())
    ) {
        response?.let {
            mockSteps.firstOrNull { step ->
                step.getStatus() == ONGOING
            }?.result = it.fromModuleApiToDomain()
        }

        handleIntentResult(
            requestCode,
            Activity.RESULT_OK,
            Intent().putExtra(FACE_BUNDLE_KEY, response))
    }

    companion object {
        private const val WRONG_REQUEST_CODE = 1
    }

}
