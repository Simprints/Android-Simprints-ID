package com.simprints.id.orchestrator.cache.crypto.response

import androidx.test.platform.app.InstrumentationRegistry
import com.simprints.id.data.secure.keystore.KeystoreManagerImpl
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureResult
import com.simprints.id.orchestrator.cache.model.FingerprintSample
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class FingerprintCaptureResponseEncoderAndroidTest {

    private val encoder by lazy {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val keystoreManager = KeystoreManagerImpl(context)
        FingerprintCaptureResponseEncoder(keystoreManager)
    }

    @Test
    fun shouldEncryptTemplate() {
        val originalResponse = mockFingerprintCaptureResponse()
        val processedResponse = encoder.process(originalResponse, Operation.ENCODE)

        require(processedResponse is FingerprintCaptureResponse)
        verifyResponses(originalResponse, processedResponse)
    }

    @Test
    fun shouldDecryptTemplate() {
        val originalResponse = encoder.process(mockFingerprintCaptureResponse(), Operation.ENCODE)
        val processedResponse = encoder.process(originalResponse, Operation.DECODE)

        require(originalResponse is FingerprintCaptureResponse)
        require(processedResponse is FingerprintCaptureResponse)
        verifyResponses(originalResponse, processedResponse)
    }

    private fun mockFingerprintCaptureResponse(): FingerprintCaptureResponse {
        val captureResult = listOf(
            FingerprintCaptureResult(
                IFingerIdentifier.RIGHT_THUMB,
                FingerprintSample(
                    "id",
                    IFingerIdentifier.RIGHT_THUMB,
                    qualityScore = 3,
                    template = "template".toByteArray(),
                    imageRef = null
                )
            )
        )
        return FingerprintCaptureResponse(captureResult = captureResult)
    }

    private fun verifyResponses(originalResponse: FingerprintCaptureResponse,
                                processedResponse: FingerprintCaptureResponse) {
        processedResponse.captureResult.forEachIndexed { index, processedFingerprint ->
            val originalTemplate = originalResponse.captureResult[index].sample?.template
            val processedTemplate = processedFingerprint.sample?.template
            assertThat(processedTemplate, not(equalTo(originalTemplate)))
        }
    }

}
