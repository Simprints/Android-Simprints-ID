package com.simprints.id.orchestrator.steps

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.exitform.ExitFormReason
import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest
import com.simprints.id.domain.moduleapi.core.requests.ConsentType
import com.simprints.id.domain.moduleapi.core.response.AskConsentResponse
import com.simprints.id.domain.moduleapi.core.response.ConsentResponse
import com.simprints.id.domain.moduleapi.core.response.CoreExitFormResponse
import com.simprints.id.domain.moduleapi.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.id.orchestrator.steps.core.CoreRequestCode
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
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

    //TODO: Add verify test once implemented

    @Test
    fun stepProcessorShouldProcessConsentResult() {
        val consentData: Intent =
            Intent().putExtra(CORE_STEP_BUNDLE, AskConsentResponse(ConsentResponse.ACCEPTED))
        val result = coreStepProcessor.processResult(consentData)

        assertThat(result).isInstanceOf(AskConsentResponse::class.java)
    }

    @Test
    fun stepProcessorShouldProcessExitFormResult() {
        val exitFormData = Intent().apply {
            putExtra(CORE_STEP_BUNDLE, CoreExitFormResponse(ExitFormReason.OTHER, "optional_text"))
        }
        val result = coreStepProcessor.processResult(exitFormData)

        assertThat(result).isInstanceOf(CoreExitFormResponse::class.java)
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}
