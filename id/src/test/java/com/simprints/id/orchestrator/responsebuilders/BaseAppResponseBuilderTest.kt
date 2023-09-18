package com.simprints.id.orchestrator.responsebuilders

import android.os.Bundle
import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.domain.moduleapi.app.responses.AppRefusalFormResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceErrorReason
import com.simprints.id.domain.moduleapi.face.responses.FaceErrorResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceExitFormResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceExitReason
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintErrorReason
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintErrorResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintRefusalFormResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintRefusalFormReason
import com.simprints.id.exitformhandler.ExitFormReason
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.response.ExitFormResponse
import com.simprints.id.orchestrator.steps.core.response.FetchGUIDResponse
import com.simprints.id.orchestrator.steps.core.response.SetupResponse
import com.simprints.infra.config.domain.models.GeneralConfiguration
import org.junit.Test

class BaseAppResponseBuilderTest {

    private val builder: BaseAppResponseBuilder = StubResponseBuilder()

    @Test
    fun `returns AppRefusalFormResponse if steps contain ExitFormResponse`() {
        val response = builder.getErrorOrRefusalResponseIfAny(wrapIntoStepList(
            ExitFormResponse(reason = ExitFormReason.APP_NOT_WORKING)
        ))

        assertThat(response).isInstanceOf(AppRefusalFormResponse::class.java)
    }

    @Test
    fun `returns AppRefusalFormResponse if steps contain FaceExitFormResponse`() {
        val response = builder.getErrorOrRefusalResponseIfAny(wrapIntoStepList(
            FaceExitFormResponse(reason = FaceExitReason.APP_NOT_WORKING, "")
        ))

        assertThat(response).isInstanceOf(AppRefusalFormResponse::class.java)
    }

    @Test
    fun `returns AppRefusalFormResponse if steps contain FingerprintRefusalFormResponse`() {
        val response = builder.getErrorOrRefusalResponseIfAny(wrapIntoStepList(
            FingerprintRefusalFormResponse(reason = FingerprintRefusalFormReason.SCANNER_NOT_WORKING, "")
        ))

        assertThat(response).isInstanceOf(AppRefusalFormResponse::class.java)
    }

    @Test
    fun `returns AppErrorResponse if steps contain FingerprintErrorResponse`() {
        val response = builder.getErrorOrRefusalResponseIfAny(wrapIntoStepList(
            FingerprintErrorResponse(FingerprintErrorReason.FINGERPRINT_CONFIGURATION_ERROR)
        ))

        assertThat(response).isInstanceOf(AppErrorResponse::class.java)
    }

    @Test
    fun `returns AppErrorResponse if steps contain FaceErrorResponse`() {
        val response = builder.getErrorOrRefusalResponseIfAny(wrapIntoStepList(
            FaceErrorResponse(FaceErrorReason.FACE_CONFIGURATION_ERROR)
        ))

        assertThat(response).isInstanceOf(AppErrorResponse::class.java)
    }

    @Test
    fun `returns AppErrorResponse if steps contain FetchGUIDResponse(false, false)`() {
        val response = builder.getErrorOrRefusalResponseIfAny(wrapIntoStepList(
            FetchGUIDResponse(isGuidFound = false, wasOnline = false)
        ))

        assertThat(response).isInstanceOf(AppErrorResponse::class.java)
    }

    @Test
    fun `returns AppErrorResponse if steps contain FetchGUIDResponse(false, true)`() {
        val response = builder.getErrorOrRefusalResponseIfAny(wrapIntoStepList(
            FetchGUIDResponse(isGuidFound = false, wasOnline = true)
        ))

        assertThat(response).isInstanceOf(AppErrorResponse::class.java)
    }

    @Test
    fun `returns null if steps contain FetchGUIDResponse(true)`() {
        val response = builder.getErrorOrRefusalResponseIfAny(wrapIntoStepList(
            FetchGUIDResponse(true)
        ))

        assertThat(response).isNull()
    }

    @Test
    fun `returns AppErrorResponse if steps contain SetupResponse(false)`() {
        val response = builder.getErrorOrRefusalResponseIfAny(wrapIntoStepList(
            SetupResponse(false)
        ))

        assertThat(response).isInstanceOf(AppErrorResponse::class.java)
    }

    @Test
    fun `returns null if steps contain SetupResponse(true)`() {
        val response = builder.getErrorOrRefusalResponseIfAny(wrapIntoStepList(
            SetupResponse(true)
        ))

        assertThat(response).isNull()
    }

    @Test
    fun `returns null if steps do not contain relevant response`() {
        val response = builder.getErrorOrRefusalResponseIfAny(emptyList())

        assertThat(response).isNull()
    }

    private fun wrapIntoStepList(result: Step.Result) = listOf(Step(
        requestCode = 0,
        activityName = "",
        payloadType = Step.PayloadType.BUNDLE,
        payload = Bundle(),
        bundleKey = "",
        status = Step.Status.ONGOING,
        result = result,
    ))

    // For this test suite we only require methods in BaseAppResponseBuilder implementation
    private class StubResponseBuilder : BaseAppResponseBuilder() {
        override suspend fun buildAppResponse(
            modalities: List<GeneralConfiguration.Modality>,
            appRequest: AppRequest,
            steps: List<Step>,
            sessionId: String
        ) = AppEnrolResponse("")
    }
}
