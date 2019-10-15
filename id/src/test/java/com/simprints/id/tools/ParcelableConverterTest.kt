package com.simprints.id.tools

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.orchestrator.cache.model.FingerprintCaptureResult
import com.simprints.id.orchestrator.cache.model.FingerprintSample
import com.simprints.id.orchestrator.steps.Step
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
class ParcelableConverterTest {

    private lateinit var converter: ParcelableConverter

    @Test
    fun withParcelableInput_shouldConvertToByteArray() {
        val bytes = mockByteArray()

        assertThat(bytes, notNullValue())
        assertThat(bytes.size, greaterThan(0))
    }

    @Test
    fun withByteArrayInput_shouldConvertToParcel() {
        val bytes = mockByteArray()
        val request = mockRequest()
        val result = mockResult()
        converter = ParcelableConverter(bytes)
        val parcel = converter.toParcel()
        val step = Step.createFromParcel(parcel)
        converter.recycle()

        assertThat(parcel, notNullValue())
        assertThat(step.requestCode, `is`(REQUEST_CODE))
        assertThat(step.activityName, `is`(ACTIVITY_NAME))
        assertThat(step.bundleKey, `is`(BUNDLE_KEY))
        assertThat(step.request, `is`(request))
        assertThat(step.getStatus(), `is`(Step.Status.COMPLETED))
        verifyResult(step.getResult(), result)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun mockByteArray(): ByteArray {
        converter = ParcelableConverter(mockParcelable())
        val bytes = converter.toBytes()
        converter.recycle()
        return bytes
    }

    private fun mockParcelable(): Step {
        val request = mockRequest()
        val result = mockResult()

        return Step(
            requestCode = REQUEST_CODE,
            activityName = ACTIVITY_NAME,
            bundleKey = BUNDLE_KEY,
            request = request,
            result = result,
            status = Step.Status.COMPLETED
        )
    }

    private fun mockRequest(): Step.Request = FingerprintCaptureRequest(
        "projectId",
        "userId",
        "moduleId",
        "metadata",
        "language",
        emptyMap(),
        true,
        "programmeName",
        "organisationName",
        emptyList()
    )

    private fun mockResult(): Step.Result {
        val captureResult = listOf(
            FingerprintCaptureResult(
                IFingerIdentifier.RIGHT_THUMB,
                FingerprintSample(
                    "id",
                    IFingerIdentifier.RIGHT_THUMB,
                    imageRef = null,
                    qualityScore = 4,
                    template = "template".toByteArray()
                )
            )
        )
        return FingerprintCaptureResponse(captureResult = captureResult)
    }

    private fun verifyResult(actual: Step.Result?, expected: Step.Result) {
        assertThat(actual, instanceOf(FingerprintCaptureResponse::class.java))
        require(actual is FingerprintCaptureResponse && expected is FingerprintCaptureResponse)
        assertThat(actual.captureResult.size, `is`(expected.captureResult.size))
        actual.captureResult.forEachIndexed { index, actualFingerprint ->
            val expectedFingerprint = expected.captureResult[index]
            expectedFingerprint.sample?.template?.let { expectedTemplate ->
                assertThat(actualFingerprint.sample?.template?.contentEquals(expectedTemplate), `is`(true))
            }
            assertThat(actualFingerprint.identifier, `is`(expectedFingerprint.identifier))
            assertThat(actualFingerprint.sample?.qualityScore, `is`(expectedFingerprint.sample?.qualityScore))
        }
    }

    companion object {
        private const val REQUEST_CODE = 123
        private const val ACTIVITY_NAME = "com.simprints.id.MyActivity"
        private const val BUNDLE_KEY = "BUNDLE_KEY"
    }

}
