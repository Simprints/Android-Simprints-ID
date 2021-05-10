package com.simprints.id.orchestrator.steps

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.id.sampledata.SampleDefaults.GUID1
import com.simprints.id.sampledata.SampleDefaults.GUID2
import com.simprints.id.data.exitform.CoreExitFormReason
import com.simprints.id.data.exitform.FaceExitFormReason
import com.simprints.id.data.exitform.FingerprintExitFormReason
import com.simprints.id.orchestrator.steps.core.response.SetupResponse
import com.simprints.id.orchestrator.steps.core.CoreRequestCode
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl
import com.simprints.id.orchestrator.steps.core.requests.AskConsentRequest
import com.simprints.id.orchestrator.steps.core.requests.ConsentType
import com.simprints.id.orchestrator.steps.core.requests.GuidSelectionRequest
import com.simprints.id.orchestrator.steps.core.response.*
import com.simprints.id.testtools.TestApplication
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class CoreStepProcessorImplTest: BaseStepProcessorTest() {

    private val coreStepProcessor = CoreStepProcessorImpl()

    @Test
    fun stepProcessor_shouldBuildRightStepForEnrol() {
        val step = CoreStepProcessorImpl().buildStepConsent(ConsentType.ENROL)

        verifyCoreIntent<AskConsentRequest>(step, CoreRequestCode.CONSENT.value)
    }

    @Test
    fun stepProcessor_shouldBuildRightStepForIdentify() {
        val step = CoreStepProcessorImpl().buildStepConsent(ConsentType.IDENTIFY)

        verifyCoreIntent<AskConsentRequest>(step, CoreRequestCode.CONSENT.value)
    }

    @Test
    fun stepProcessor_shouldProcessFetchGUIDResponse() {
        val fetchActivityReturn: Intent =
            Intent().putExtra(CORE_STEP_BUNDLE, FetchGUIDResponse(false))
        val result = coreStepProcessor.processResult(fetchActivityReturn)

        assertThat(result).isInstanceOf(FetchGUIDResponse::class.java)
    }

    @Test
    fun stepProcessor_shouldBuildRightStepForVerify() {
        val step = CoreStepProcessorImpl().buildStepConsent(ConsentType.VERIFY)

        verifyCoreIntent<AskConsentRequest>(step, CoreRequestCode.CONSENT.value)
    }

    @Test
    fun buildConfirmIdentityStepShouldBuildTheRightStep() {
        val step = coreStepProcessor.buildConfirmIdentityStep(DEFAULT_PROJECT_ID, GUID1, GUID2)
        with(step) {
            assertThat(activityName).isEqualTo(GUID_SELECTION_ACTIVITY_NAME)
            assertThat(requestCode).isEqualTo(GUID_SELECTION_REQUEST_CODE)
            assertThat(bundleKey).isEqualTo(CORE_STEP_BUNDLE)
            assertThat(request).isEqualTo(GuidSelectionRequest(DEFAULT_PROJECT_ID, GUID1, GUID2))
            assertThat(getStatus()).isEqualTo(Step.Status.NOT_STARTED)
        }
    }

    @Test
    fun stepProcessor_shouldProcessGuidSelectionResponse() {
        val guidSelectionReturn: Intent =
            Intent().putExtra(CORE_STEP_BUNDLE, GuidSelectionResponse(true))
        val result = coreStepProcessor.processResult(guidSelectionReturn)

        assertThat(result).isInstanceOf(GuidSelectionResponse::class.java)
    }

    @Test
    fun stepProcessor_shouldProcessConsentResult() {
        val consentData: Intent =
            Intent().putExtra(CORE_STEP_BUNDLE, AskConsentResponse(ConsentResponse.ACCEPTED))
        val result = coreStepProcessor.processResult(consentData)

        assertThat(result).isInstanceOf(AskConsentResponse::class.java)
    }

    @Test
    fun stepProcessor_shouldProcessCoreExitFormResult() {
        val exitFormData = Intent().apply {
            putExtra(CORE_STEP_BUNDLE, CoreExitFormResponse(CoreExitFormReason.OTHER, "optional_text"))
        }
        val result = coreStepProcessor.processResult(exitFormData)

        assertThat(result).isInstanceOf(CoreExitFormResponse::class.java)
    }

    @Test
    fun stepProcessor_shouldProcessFingerprintExitFormResult() {
        val fingerprintExitFormData = Intent().apply {
            putExtra(CORE_STEP_BUNDLE,
                CoreFingerprintExitFormResponse(FingerprintExitFormReason.OTHER, "fingerprint_optional_text"))
        }
        val result = coreStepProcessor
            .processResult(fingerprintExitFormData)

        assertThat(result).isInstanceOf(CoreFingerprintExitFormResponse::class.java)
    }

    @Test
    fun stepProcessor_shouldProcessFaceExitFormResult() {
        val faceExitFormData = Intent().apply {
            putExtra(CORE_STEP_BUNDLE, CoreFaceExitFormResponse(FaceExitFormReason.OTHER,
                "face_optional_text"))
        }
        val result = coreStepProcessor.processResult(faceExitFormData)

        assertThat(result).isInstanceOf(CoreFaceExitFormResponse::class.java)
    }

    @Test
    fun stepProcessor_shouldProcessResultFromSetup() {
        val setupData = Intent().apply {
            putExtra(CORE_STEP_BUNDLE, SetupResponse(true))
        }

        val result = coreStepProcessor.processResult(setupData)

        assertThat(result).isInstanceOf(SetupResponse::class.java)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    companion object {
        const val GUID_SELECTION_ACTIVITY_NAME = "com.simprints.id.activities.guidselection.GuidSelectionActivity"
        const val GUID_SELECTION_REQUEST_CODE = 304
        const val CORE_STEP_BUNDLE = "core_step_bundle"
    }
}
