package com.simprints.id.tools

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.orchestrator.cache.model.Fingerprint
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
        assertThat(step.getResult(), `is`(result))
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
        mapOf(),
        true,
        "programmeName",
        "organisationName",
        "activityTitle"
    )

    private fun mockResult(): Step.Result {
        val fingerprints = listOf(
            Fingerprint(
                IFingerIdentifier.RIGHT_THUMB,
                "template".toByteArray(),
                qualityScore = 3
            )
        )
        return FingerprintCaptureResponse(fingerprints)
    }

    companion object {
        private const val REQUEST_CODE = 123
        private const val ACTIVITY_NAME = "com.simprints.id.MyActivity"
        private const val BUNDLE_KEY = "BUNDLE_KEY"
    }

}
