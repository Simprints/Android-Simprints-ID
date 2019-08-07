package com.simprints.id.orchestrator

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.os.Parcelable
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequestAction.*
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.face.FaceToDomainResponse.fromFaceToDomainResponse
import com.simprints.id.domain.moduleapi.face.requests.FaceEnrolRequest
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintToDomainResponse.fromFingerprintToDomainResponse
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import com.simprints.id.orchestrator.builders.AppResponseFactory
import com.simprints.id.orchestrator.modality.ModalityFlow
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Request
import com.simprints.id.orchestrator.steps.Step.Result
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.Step.Status.ONGOING
import com.simprints.id.orchestrator.steps.face.BaseFaceStepProcessor
import com.simprints.id.orchestrator.steps.face.BaseFaceStepProcessor.Companion.FACE_ENROL_REQUEST_CODE
import com.simprints.id.orchestrator.steps.face.BaseFaceStepProcessor.Companion.FACE_IDENTIFY_REQUEST_CODE
import com.simprints.id.orchestrator.steps.face.BaseFaceStepProcessor.Companion.FACE_VERIFY_REQUEST_CODE
import com.simprints.id.orchestrator.steps.face.BaseFaceStepProcessor.Companion.isFaceResult
import com.simprints.id.orchestrator.steps.fingerprint.BaseFingerprintStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.BaseFingerprintStepProcessor.Companion.FINGERPRINT_ENROL_REQUEST_CODE
import com.simprints.id.orchestrator.steps.fingerprint.BaseFingerprintStepProcessor.Companion.FINGERPRINT_IDENTIFY_REQUEST_CODE
import com.simprints.id.orchestrator.steps.fingerprint.BaseFingerprintStepProcessor.Companion.FINGERPRINT_VERIFY_REQUEST_CODE
import com.simprints.id.orchestrator.steps.fingerprint.BaseFingerprintStepProcessor.Companion.isFingerprintResult
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.moduleapi.face.responses.IFaceEnrolResponse
import com.simprints.moduleapi.face.responses.IFaceIdentifyResponse
import com.simprints.moduleapi.face.responses.IFaceVerifyResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintEnrolResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintIdentifyResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintVerifyResponse
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.verifyOnce
import com.simprints.testtools.common.syntax.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.stubbing.Answer
import com.simprints.moduleapi.face.responses.IFaceResponse.Companion.BUNDLE_KEY as FACE_BUNDLE_KEY
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse.Companion.BUNDLE_KEY as FINGERPRINT_BUNDLE_KEY

@RunWith(AndroidJUnit4::class)
class OrchestratorManagerImplTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private lateinit var appResponseFactoryMock: AppResponseFactory
    private lateinit var modalityFlowMock: ModalityFlow
    private val mockSteps = mutableListOf<Step>()

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

        intending(toPackage(BaseFingerprintStepProcessor.ACTIVITY_CLASS_NAME)).respondWith(ActivityResult(Activity.RESULT_OK, null))
        intending(toPackage(BaseFaceStepProcessor.ACTIVITY_CLASS_NAME)).respondWith(ActivityResult(Activity.RESULT_OK, null))
    }

    @Test
    fun orchestratorStarts_shouldGetFirstStepFromModalityFlow() {
        val modalities = listOf(FACE)

        with(buildOrchestratorManager()) {
            prepareModalFlowFor(modalities, appEnrolRequest)

            runBlocking {
                startFlow(modalities)
            }

            verifyOrchestratorGotNextStepFromModalityFlow()
        }
    }

    @Test
    fun modalityFlowCompletes_orchestratorShouldTryToBuildAppResponse() {
        val modalities = listOf(FACE)

        with(buildOrchestratorManager()) {
            prepareModalFlowFor(modalities, appEnrolRequest)

            runBlocking {
                startFlow(modalities)
                progressWithResult(FaceEnrolResponse(SOME_GUID))
            }

            verifyOrchestratorTriedToBuildAppResponse()
        }
    }

    @Test
    fun orchestratorReceivesAResult_itShouldBeForwardedToModalityFlowAndMoveOn() {
        val modalities = listOf(FACE)

        with(buildOrchestratorManager()) {
            prepareModalFlowFor(modalities, appEnrolRequest)

            runBlocking {
                startFlow(modalities)
                progressWithResult(FaceEnrolResponse(SOME_GUID))
            }

            verifyOrchestratorForwardedResultsToModalityFlow()
            verifyOrchestratorGotNextStepFromModalityFlow(2)
        }
    }

    private fun verifyOrchestratorGotNextStepFromModalityFlow(nTimes: Int = 1) =
        verify(modalityFlowMock, times(nTimes)).getNextStepToStart()

    private fun verifyOrchestratorForwardedResultsToModalityFlow() =
        verifyOnce(modalityFlowMock) { handleIntentResult(anyInt(), anyInt(), anyNotNull()) }

    private fun verifyOrchestratorTriedToBuildAppResponse() =
        verifyOnce(appResponseFactoryMock) { buildAppResponse(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }

    private fun prepareModalFlowFor(modalities: List<Modality>, appRequest: AppRequest) {
        whenever(modalityFlowMock) { getNextStepToStart() } thenAnswer Answer { mockSteps.firstOrNull { it.status == NOT_STARTED } }

        modalities.forEach {
            if (it == FACE) {
                when (AppRequest.action(appRequest)) {
                    ENROL -> FACE_ENROL_REQUEST_CODE
                    VERIFY -> FACE_VERIFY_REQUEST_CODE
                    IDENTIFY -> FACE_IDENTIFY_REQUEST_CODE
                    CONFIRM -> TODO()
                }
            } else {
                when (AppRequest.action(appRequest)) {
                    ENROL -> FINGERPRINT_ENROL_REQUEST_CODE
                    VERIFY -> FINGERPRINT_VERIFY_REQUEST_CODE
                    IDENTIFY -> FINGERPRINT_IDENTIFY_REQUEST_CODE
                    CONFIRM -> TODO()
                }
            }.also { code ->
                mockSteps.add(Step(Request(code, Intent()), NOT_STARTED))
            }
        }
    }

//    @Test
//    fun startMultiFlowModality_orchestratorShouldInvokeTheRightIntent() {
//        with(buildOrchestratorManager(FACE_FINGER)) {
//
//            runBlocking {
//                startFlow()
//            }
//
//            checkOnGoingStepIsForFace()
//        }
//    }
//
//    @Test
//    fun modalityHasFinished_orchestratorShouldProduceAnAppResponse() {
//        with(buildOrchestratorManager(FACE)) {
//
//            runBlocking {
//                startFlow()
//                progressWithResult(FaceEnrolResponse(SOME_GUID))
//            }
//
//            checkAppResponse(AppEnrolResponse(SOME_GUID))
//        }
//    }
//
//    @Test
//    fun modalityNotFinishedYet_orchestratorShouldInvokeNextIntent() {
//        with(buildOrchestratorManager(FACE_FINGER)) {
//
//            runBlocking {
//                startFlow()
//                progressWithResult(FaceEnrolResponse(SOME_GUID))
//            }
//
//            checkNextIntentIsForFingerprint()
//        }
//    }

    private fun buildOrchestratorManager(): OrchestratorManager {
        val modalityFlowFactoryMock = mock<ModalityFlowFactory>().apply {
            whenever(this) { startModalityFlow(any(), any()) } thenReturn modalityFlowMock
        }

        return OrchestratorManagerImpl(modalityFlowFactoryMock, appResponseFactoryMock)
    }

    private fun OrchestratorManager.checkOnGoingStepIsForFace() =
        assertThat(isFaceResult(onGoingStep.value?.request?.requestCode!!)).isTrue()

    private fun OrchestratorManager.checkNextIntentIsForFingerprint() =
        assertThat(isFingerprintResult(onGoingStep.value?.request?.requestCode!!)).isTrue()


    private fun OrchestratorManager.checkAppResponse(response: AppResponse) {
        assertThat(appResponse.value).isEqualTo(response)
    }

    private suspend fun OrchestratorManager.startFlow(
        modalities: List<Modality>,
        request: AppRequest = appEnrolRequest,
        sessionId: String = "") = start(modalities, request, sessionId)

    private suspend fun OrchestratorManager.progressWithResult(response: Parcelable) {

        val params: Triple<Int, String, Result> = when (response) {
            is IFaceEnrolResponse -> Triple(FACE_ENROL_REQUEST_CODE, FACE_BUNDLE_KEY, fromFaceToDomainResponse(response))
            is IFaceVerifyResponse -> Triple(FACE_VERIFY_REQUEST_CODE, FACE_BUNDLE_KEY, fromFaceToDomainResponse(response))
            is IFaceIdentifyResponse -> Triple(FACE_IDENTIFY_REQUEST_CODE, FACE_BUNDLE_KEY, fromFaceToDomainResponse(response))
            is IFingerprintEnrolResponse -> Triple(FINGERPRINT_ENROL_REQUEST_CODE, FINGERPRINT_BUNDLE_KEY, fromFingerprintToDomainResponse(response))
            is IFingerprintIdentifyResponse -> Triple(FINGERPRINT_IDENTIFY_REQUEST_CODE, FINGERPRINT_BUNDLE_KEY, fromFingerprintToDomainResponse(response))
            is IFingerprintVerifyResponse -> Triple(FINGERPRINT_VERIFY_REQUEST_CODE, FINGERPRINT_BUNDLE_KEY, fromFingerprintToDomainResponse(response))
            else -> throw Throwable("Invald Response")
        }

        mockSteps.firstOrNull { it.status == ONGOING }?.result = params.third
        onModalStepRequestDone(
            params.first,
            Activity.RESULT_OK,
            Intent().putExtra(params.second, response))
    }

    companion object {
        private const val PACKAGE_NAME = "com.simprints.id"
        private const val SOME_GUID = "some_guid"
        private const val SOME_PROJECT_ID = "some_project"
        private const val SOME_USER_ID = "some_user"
        private const val SOME_MODULE_ID = "some_module"

        private val faceEnrolRequest = FaceEnrolRequest(SOME_PROJECT_ID, SOME_USER_ID, SOME_MODULE_ID)
        private val fingerprintEnrolRequest: FingerprintEnrolRequest =
            FingerprintEnrolRequest(SOME_PROJECT_ID, SOME_USER_ID, SOME_MODULE_ID, "", "", mapOf(), false, "", "")
    }
}
