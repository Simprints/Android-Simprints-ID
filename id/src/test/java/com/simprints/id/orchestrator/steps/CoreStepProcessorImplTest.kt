package com.simprints.id.orchestrator.steps

import android.content.Intent
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.consent.ConsentContract
import com.simprints.feature.consent.ConsentResult
import com.simprints.feature.consent.ConsentType
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.exitform.config.ExitFormOption
import com.simprints.id.data.exitform.ExitFormReason
import com.simprints.id.orchestrator.steps.core.CoreRequestCode
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl
import com.simprints.id.orchestrator.steps.core.requests.GuidSelectionRequest
import com.simprints.id.orchestrator.steps.core.response.*
import com.simprints.id.testtools.TestApplication
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class CoreStepProcessorImplTest : BaseStepProcessorTest() {

    private val coreStepProcessor = CoreStepProcessorImpl()

    @Test
    fun stepProcessor_shouldBuildRightStepForEnrol() {
        val step = CoreStepProcessorImpl().buildStepConsent(ConsentType.ENROL)

        verifyConsentIntent<Bundle>(step, CoreRequestCode.CONSENT.value)
    }

    @Test
    fun stepProcessor_shouldBuildRightStepForIdentify() {
        val step = CoreStepProcessorImpl().buildStepConsent(ConsentType.IDENTIFY)

        verifyConsentIntent<Bundle>(step, CoreRequestCode.CONSENT.value)
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

        verifyConsentIntent<Bundle>(step, CoreRequestCode.CONSENT.value)
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
        val guidSelectionReturn = Intent().putExtra(CORE_STEP_BUNDLE, GuidSelectionResponse(true))
        val result = coreStepProcessor.processResult(guidSelectionReturn)

        assertThat(result).isInstanceOf(GuidSelectionResponse::class.java)
    }

    @Test
    fun stepProcessor_shouldSkipLegacyNavigationConsentResult() {
        val consentData = Intent().putExtra(CORE_STEP_BUNDLE, AskConsentResponse(ConsentResponse.ACCEPTED))
        val result = coreStepProcessor.processResult(consentData)

        assertThat(result).isNull()
    }

    @Test
    fun stepProcessor_shouldReturnConsentResultWhenAccepted() {
        val consentData = Intent().putExtra(ConsentContract.CONSENT_RESULT, ConsentResult(true))
        val result = coreStepProcessor.processResult(consentData)

        assertThat(result).isInstanceOf(AskConsentResponse::class.java)
    }

    @Test
    fun stepProcessor_shouldReturnConsentResultWhenExitFormSubmitted() {
        val consentData = Intent().putExtra(ConsentContract.CONSENT_RESULT, ExitFormResult(true, ExitFormOption.Other))
        val result = coreStepProcessor.processResult(consentData)

        assertThat(result).isInstanceOf(ExitFormResponse::class.java)
    }

    @Test
    fun stepProcessor_shouldReturnNullWhenExitFormNotSubmitted() {
        val consentData = Intent().putExtra(ConsentContract.CONSENT_RESULT, ExitFormResult(false))
        val result = coreStepProcessor.processResult(consentData)

        assertThat(result).isNull()
    }

    @Test
    fun stepProcessor_shouldProcessCoreExitFormResult() {
        val exitFormData = Intent().putExtra(
            CORE_STEP_BUNDLE,
            ExitFormResponse(ExitFormReason.OTHER, "optional_text")
        )
        val result = coreStepProcessor.processResult(exitFormData)

        assertThat(result).isInstanceOf(ExitFormResponse::class.java)
    }

    @Test
    fun stepProcessor_shouldProcessResultFromSetup() {
        val setupData = Intent().putExtra(CORE_STEP_BUNDLE, SetupResponse(true))
        val result = coreStepProcessor.processResult(setupData)

        assertThat(result).isInstanceOf(SetupResponse::class.java)
    }


    @Test
    fun stepProcessor_shouldReturnNullWhenNoResponseInData() {
        val setupData = Intent().putExtra("invalidKey", "invalidData")
        val result = coreStepProcessor.processResult(setupData)

        assertThat(result).isNull()
    }

    @Test
    fun stepProcessor_shouldReturnNullWhenNoValidResponseInData() {
        val result = coreStepProcessor.processResult(Intent())

        assertThat(result).isNull()
    }

    companion object {
        const val GUID_SELECTION_ACTIVITY_NAME =
            "com.simprints.id.activities.guidselection.GuidSelectionActivity"
        const val GUID_SELECTION_REQUEST_CODE = 304
        const val CORE_STEP_BUNDLE = "core_step_bundle"
    }
}
