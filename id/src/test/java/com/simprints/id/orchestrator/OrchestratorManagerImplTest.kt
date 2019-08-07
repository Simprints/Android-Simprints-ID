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
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.*
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.requests.FaceEnrolRequest
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import com.simprints.id.orchestrator.builders.AppResponseFactoryImpl
import com.simprints.id.orchestrator.modality.flows.FaceModalityFlow
import com.simprints.id.orchestrator.modality.flows.FaceModalityFlow.Companion.REQUEST_CODE_FACE
import com.simprints.id.orchestrator.modality.flows.FaceModalityFlow.Companion.faceActivityClassName
import com.simprints.id.orchestrator.modality.flows.FingerprintModalityFlow
import com.simprints.id.orchestrator.modality.flows.FingerprintModalityFlow.Companion.REQUEST_CODE_FINGERPRINT
import com.simprints.id.orchestrator.modality.flows.FingerprintModalityFlow.Companion.fingerprintActivityClassName
import com.simprints.id.orchestrator.modality.flows.MultiModalitiesFlowBase
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.moduleapi.face.responses.IFaceResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class OrchestratorManagerImplTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Mock private lateinit var modalityFlowFactoryMock: ModalityFlowFactory
    @Mock private lateinit var modalityFlowMock: ModalityFlow

    private val appRequest = AppEnrolRequest("some_project_id", "some_user_id", "some_module_id", "some_metadata")

    @Before
    fun setUp() {
        UnitTestConfig(this)
            .coroutinesMainThread()
            .rescheduleRxMainThread()

        MockitoAnnotations.initMocks(this)
        Intents.init()
    }

    @Test
    fun startSingleFlowModality_orchestratorShouldInvokeTheRightIntent() {
        with(buildOrchestratorManager(FACE)) {

            runBlocking {
                startFlow()
            }

            checkNextIntentIsForFace()
        }
    }

    @Test
    fun startMultiFlowModality_orchestratorShouldInvokeTheRightIntent() {
        with(buildOrchestratorManager(FACE_FINGER)) {

            runBlocking {
                startFlow()
            }

            checkNextIntentIsForFace()
        }
    }

    @Test
    fun modalityHasFinished_orchestratorShouldProduceAnAppResponse() {
        with(buildOrchestratorManager(FACE)) {

            runBlocking {
                startFlow()
                progressWithResult(FaceEnrolResponse(SOME_GUID))
            }

            checkAppResponse(AppEnrolResponse(SOME_GUID))
        }
    }

    @Test
    fun modalityNotFinishedYet_orchestratorShouldInvokeNextIntent() {
        with(buildOrchestratorManager(FACE_FINGER)) {

            runBlocking {
                startFlow()
                progressWithResult(FaceEnrolResponse(SOME_GUID))
            }

            checkNextIntentIsForFingerprint()
        }
    }

    private fun buildOrchestratorManager(modality: Modality): OrchestratorManager {
        intending(toPackage(faceActivityClassName)).respondWith(ActivityResult(Activity.RESULT_OK, null))
        intending(toPackage(fingerprintActivityClassName)).respondWith(ActivityResult(Activity.RESULT_OK, null))

        when (modality) {
            FACE -> listOf(
                FaceModalityFlow(appRequest, PACKAGE_NAME, mock<FaceRequestFactory>().apply {
                    whenever(this) { buildFaceRequest(anyNotNull()) } thenReturn faceEnrolRequest
                }))
            FINGER_FACE -> listOf(
                FingerprintModalityFlow(appRequest, PACKAGE_NAME, mock<FingerprintRequestFactory>().apply {
                    whenever(this) { buildFingerprintRequest(anyNotNull(), anyNotNull()) } thenReturn fingerprintEnrolRequest
                }, mock()),
                FaceModalityFlow(appRequest, PACKAGE_NAME, mock<FaceRequestFactory>().apply {
                    whenever(this) { buildFaceRequest(anyNotNull()) } thenReturn faceEnrolRequest
                }))
            FACE_FINGER -> listOf(
                FaceModalityFlow(appRequest, PACKAGE_NAME, mock<FaceRequestFactory>().apply {
                    whenever(this) { buildFaceRequest(anyNotNull()) } thenReturn faceEnrolRequest
                }),
                FingerprintModalityFlow(appRequest, PACKAGE_NAME, mock<FingerprintRequestFactory>().apply {
                    whenever(this) { buildFingerprintRequest(anyNotNull(), anyNotNull()) } thenReturn fingerprintEnrolRequest
                }, mock()))
            FINGER -> listOf(
                FingerprintModalityFlow(appRequest, PACKAGE_NAME, mock<FingerprintRequestFactory>().apply {
                    whenever(this) { buildFingerprintRequest(anyNotNull(), anyNotNull()) } thenReturn fingerprintEnrolRequest
                }, mock()))
        }.let { modalities ->
            whenever(modalityFlowFactoryMock) { startModalityFlow(anyNotNull(), anyNotNull()) } thenReturn MultiModalitiesFlowBase(modalities)
        }

        return OrchestratorManagerImpl(modality, modalityFlowFactoryMock, AppResponseFactoryImpl())
    }

    private fun OrchestratorManager.checkNextIntentIsForFace() = checkNextIntent(REQUEST_CODE_FACE)
    private fun OrchestratorManager.checkNextIntentIsForFingerprint() = checkNextIntent(REQUEST_CODE_FINGERPRINT)
    private fun OrchestratorManager.checkNextIntent(requestCode: Int) {
        assertThat(nextIntent.value?.requestCode).isEqualTo(requestCode)
    }

    private fun OrchestratorManager.checkAppResponse(response: AppResponse) {
        assertThat(appResponse.value).isEqualTo(response)
    }

    private suspend fun OrchestratorManager.startFlow(request: AppRequest = appRequest, sessionId: String = "") {
        start(request, sessionId)
    }

    private suspend fun OrchestratorManager.progressWithResult(response: Parcelable) {
        val requestCode = if (response is IFaceResponse) {
            REQUEST_CODE_FACE
        } else {
            REQUEST_CODE_FINGERPRINT
        }

        val bundleKey = if (response is IFaceResponse) {
            IFaceResponse.BUNDLE_KEY
        } else {
            IFingerprintResponse.BUNDLE_KEY
        }

        onModalStepRequestDone(
            requestCode,
            Activity.RESULT_OK,
            Intent().putExtra(bundleKey, response))
    }

    companion object {
        private const val PACKAGE_NAME = "com.simprints.id"
        private const val SOME_GUID = "some_guid"
        private const val SOME_PROJECT_ID = "some_project"
        private const val SOME_USER_ID = "some_user"
        private const val SOME_MODULE_ID = "some_module"

        private val faceEnrolRequest = FaceEnrolRequest(SOME_PROJECT_ID, SOME_USER_ID, SOME_MODULE_ID)
        private val fingerprintEnrolRequest:FingerprintEnrolRequest =
            FingerprintEnrolRequest(SOME_PROJECT_ID, SOME_USER_ID, SOME_MODULE_ID, "", "", mapOf(), false, "", "")
    }
}
