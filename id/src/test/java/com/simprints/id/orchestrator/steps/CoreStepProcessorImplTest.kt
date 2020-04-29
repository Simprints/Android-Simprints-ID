package com.simprints.id.orchestrator.steps

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.DefaultTestConstants.GUID2
import com.simprints.id.data.exitform.CoreExitFormReason
import com.simprints.id.data.exitform.FaceExitFormReason
import com.simprints.id.data.exitform.FingerprintExitFormReason
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
    fun stepProcessorShouldBuildRightStepForEnrol() {
        val step = CoreStepProcessorImpl().buildStepConsent(ConsentType.ENROL)

        verifyCoreIntent<AskConsentRequest>(step, CoreRequestCode.CONSENT.value)
    }

    @Test
    fun stepProcessorShouldBuildRightStepForIdentify() {
        val step = CoreStepProcessorImpl().buildStepConsent(ConsentType.IDENTIFY)

        verifyCoreIntent<AskConsentRequest>(step, CoreRequestCode.CONSENT.value)
    }

    @Test
    fun stepProcessorShouldProcessFetchGUIDResponse() {
        val fetchActivityReturn: Intent =
            Intent().putExtra(CORE_STEP_BUNDLE, FetchGUIDResponse(false))
        val result = coreStepProcessor.processResult(fetchActivityReturn)

        assertThat(result).isInstanceOf(FetchGUIDResponse::class.java)
    }

    @Test
    fun buildIdentityConfirmationStepShouldBuildTheRightStep() {
        val step = coreStepProcessor.buildIdentityConfirmationStep(DEFAULT_PROJECT_ID, GUID1, GUID2)
        with(step) {
            assertThat(activityName).isEqualTo(GUID_SELECTION_ACTIVITY_NAME)
            assertThat(requestCode).isEqualTo(GUID_SELECTION_REQUEST_CODE)
            assertThat(bundleKey).isEqualTo(CORE_STEP_BUNDLE)
            assertThat(request).isEqualTo(GuidSelectionRequest(DEFAULT_PROJECT_ID, GUID1, GUID2))
            assertThat(getStatus()).isEqualTo(Step.Status.NOT_STARTED)
        }
    }

    @Test
    fun stepProcessorShouldProcessGuidSelectionResponse() {
        val guidSelectionReturn: Intent =
            Intent().putExtra(CORE_STEP_BUNDLE, GuidSelectionResponse(true))
        val result = coreStepProcessor.processResult(guidSelectionReturn)

        assertThat(result).isInstanceOf(GuidSelectionResponse::class.java)
    }

    @Test
    fun stepProcessorShouldProcessConsentResult() {
        val consentData: Intent =
            Intent().putExtra(CORE_STEP_BUNDLE, AskConsentResponse(ConsentResponse.ACCEPTED))
        val result = coreStepProcessor.processResult(consentData)

        assertThat(result).isInstanceOf(AskConsentResponse::class.java)
    }

    @Test
    fun stepProcessorShouldProcessCoreExitFormResult() {
        val exitFormData = Intent().apply {
            putExtra(CORE_STEP_BUNDLE, CoreExitFormResponse(CoreExitFormReason.OTHER, "optional_text"))
        }
        val result = coreStepProcessor.processResult(exitFormData)

        assertThat(result).isInstanceOf(CoreExitFormResponse::class.java)
    }

    @Test
    fun stepProcessorShouldProcessFingerprintExitFormResult() {
        val fingerprintExitFormData = Intent().apply {
            putExtra(CORE_STEP_BUNDLE,
                CoreFingerprintExitFormResponse(FingerprintExitFormReason.OTHER, "fingerprint_optional_text"))
        }
        val result = coreStepProcessor
            .processResult(fingerprintExitFormData)

        assertThat(result).isInstanceOf(CoreFingerprintExitFormResponse::class.java)
    }

    @Test
    fun stepProcessorShouldProcessFaceExitFormResult() {
        val faceExitFormData = Intent().apply {
            putExtra(CORE_STEP_BUNDLE, CoreFaceExitFormResponse(FaceExitFormReason.OTHER,
                "face_optional_text"))
        }
        val result = coreStepProcessor.processResult(faceExitFormData)

        assertThat(result).isInstanceOf(CoreFaceExitFormResponse::class.java)
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
