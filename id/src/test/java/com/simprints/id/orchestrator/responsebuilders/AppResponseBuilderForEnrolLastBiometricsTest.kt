package com.simprints.id.orchestrator.responsebuilders

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.orchestrator.SOME_GUID
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreRequestCode
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl
import com.simprints.id.orchestrator.steps.core.requests.EnrolLastBiometricsRequest
import com.simprints.id.orchestrator.steps.core.response.CoreResponse
import com.simprints.id.orchestrator.steps.core.response.EnrolLastBiometricsResponse
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class AppResponseBuilderForEnrolLastBiometricsTest {

    @Test
    fun givenAnAppBuilder_withAValidLastBiometricStepResult_shouldBuildAppResponse() {
        runBlockingTest {
            val lastEnBuilderForEnrolLastBiometrics = AppResponseBuilderForEnrolLastBiometrics()
            val step = Step(
                requestCode = CoreRequestCode.LAST_BIOMETRICS_CORE.value,
                activityName = CoreStepProcessorImpl.LAST_BIOMETRICS_CORE_ACTIVITY_NAME,
                bundleKey = CoreResponse.CORE_STEP_BUNDLE,
                request = EnrolLastBiometricsRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, emptyList(), SOME_GUID),
                status = Step.Status.NOT_STARTED
            )
            step.setResult(EnrolLastBiometricsResponse(SOME_GUID))
            val response = lastEnBuilderForEnrolLastBiometrics.buildAppResponse(mockk(), mockk(), listOf(step), SOME_GUID) as AppEnrolResponse

            assertThat(response.guid).isEqualTo(SOME_GUID)
        }
    }

    @Test
    fun givenAnAppBuilder_withAnInvalidLastBiometricStepResult_shouldBuildAppResponse() {
        runBlockingTest {
            val lastEnBuilderForEnrolLastBiometrics = AppResponseBuilderForEnrolLastBiometrics()
            val step = Step(
                requestCode = CoreRequestCode.LAST_BIOMETRICS_CORE.value,
                activityName = CoreStepProcessorImpl.LAST_BIOMETRICS_CORE_ACTIVITY_NAME,
                bundleKey = CoreResponse.CORE_STEP_BUNDLE,
                request = EnrolLastBiometricsRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, emptyList(), SOME_GUID),
                status = Step.Status.NOT_STARTED
            )
            val response = lastEnBuilderForEnrolLastBiometrics.buildAppResponse(mockk(), mockk(), listOf(step), SOME_GUID)

            assertThat(response).isInstanceOf(AppErrorResponse::class.java)
        }
    }
}
