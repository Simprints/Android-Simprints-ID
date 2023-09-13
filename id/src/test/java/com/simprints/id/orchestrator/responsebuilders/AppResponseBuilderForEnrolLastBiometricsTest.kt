package com.simprints.id.orchestrator.responsebuilders

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreRequestCode
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl
import com.simprints.id.orchestrator.steps.core.requests.EnrolLastBiometricsRequest
import com.simprints.id.orchestrator.steps.core.response.CoreResponse
import com.simprints.id.orchestrator.steps.core.response.EnrolLastBiometricsResponse
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AppResponseBuilderForEnrolLastBiometricsTest {

    @Test
    fun givenAnAppBuilder_withAValidLastBiometricStepResult_shouldBuildAppResponse() {
        runTest {
            val lastEnBuilderForEnrolLastBiometrics = AppResponseBuilderForEnrolLastBiometrics()
            val step = Step(
                requestCode = CoreRequestCode.LAST_BIOMETRICS_CORE.value,
                activityName = CoreStepProcessorImpl.LAST_BIOMETRICS_CORE_ACTIVITY_NAME,
                bundleKey = CoreResponse.CORE_STEP_BUNDLE,
                payloadType = Step.PayloadType.REQUEST,
                payload = EnrolLastBiometricsRequest(
                    projectId = DEFAULT_PROJECT_ID,
                    userId = DEFAULT_USER_ID.value,
                    moduleId = DEFAULT_MODULE_ID.value,
                    previousSteps = emptyList(),
                    sessionId = GUID1
                ),
                status = Step.Status.NOT_STARTED
            )
            step.setResult(EnrolLastBiometricsResponse(GUID1))
            val response = lastEnBuilderForEnrolLastBiometrics.buildAppResponse(
                mockk(),
                mockk(),
                listOf(step),
                GUID1
            ) as AppEnrolResponse

            assertThat(response.guid).isEqualTo(GUID1)
        }
    }

    @Test
    fun givenAnAppBuilder_withAnInvalidLastBiometricStepResult_shouldBuildAppResponse() {
        runTest {
            val lastEnBuilderForEnrolLastBiometrics = AppResponseBuilderForEnrolLastBiometrics()
            val step = Step(
                requestCode = CoreRequestCode.LAST_BIOMETRICS_CORE.value,
                activityName = CoreStepProcessorImpl.LAST_BIOMETRICS_CORE_ACTIVITY_NAME,
                bundleKey = CoreResponse.CORE_STEP_BUNDLE,
                payloadType = Step.PayloadType.REQUEST,
                payload = EnrolLastBiometricsRequest(
                    projectId = DEFAULT_PROJECT_ID,
                    userId = DEFAULT_USER_ID.value,
                    moduleId = DEFAULT_MODULE_ID.value,
                    previousSteps = emptyList(),
                    sessionId = GUID1
                ),
                status = Step.Status.NOT_STARTED
            )
            val response = lastEnBuilderForEnrolLastBiometrics.buildAppResponse(
                mockk(),
                mockk(),
                listOf(step),
                GUID1
            )

            assertThat(response).isInstanceOf(AppErrorResponse::class.java)
        }
    }
}
