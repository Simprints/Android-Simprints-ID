package com.simprints.id.orchestrator.steps

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.orchestrator.steps.Step.Status.COMPLETED
import com.simprints.id.orchestrator.steps.Step.Status.ONGOING
import com.simprints.id.orchestrator.steps.core.requests.CoreRequest
import com.simprints.moduleapi.face.requests.IFaceCaptureRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintCaptureRequest
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.mockk
import org.junit.Test

class StepTest {

    @Test
    fun resultSet_stepShouldUpdateTheState() {
        val resultOk = 0
        val step = Step(
            requestCode = resultOk,
            activityName = "someActivityClassName",
            bundleKey = "bundle_key",
            payloadType = Step.PayloadType.REQUEST,
            payload = mockk(),
            result = mockk(),
            status = ONGOING
        )
        assertThat(step.getStatus()).isEqualTo(COMPLETED)
    }

    @Test
    fun `should return IFingerprintRequest when request is a FingerprintRequest`() {
        val fingerprintRequest = mockk<FingerprintCaptureRequest>()
        val parcelable = fingerprintRequest.fromDomainToModuleApi()

        assertThat(parcelable).isInstanceOf( IFingerprintCaptureRequest::class.java)
    }

    @Test
    fun `should return the same object when request is a CoreRequest`() {
        val coreRequest = mockk<CoreRequest>()
        val parcelable = coreRequest.fromDomainToModuleApi()

        assertThat(parcelable).isEqualTo( coreRequest)
    }

    @Test
    fun `should throw an exception when this is an invalid request`() {
        val invalidRequest = mockk<Step.Request>()

        assertThrows<Throwable> {
            invalidRequest.fromDomainToModuleApi()
        }
    }

}
