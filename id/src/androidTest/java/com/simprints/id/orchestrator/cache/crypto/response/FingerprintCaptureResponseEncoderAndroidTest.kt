package com.simprints.id.orchestrator.cache.crypto.response

import androidx.test.platform.app.InstrumentationRegistry
import com.simprints.id.data.secure.keystore.KeystoreManagerImpl
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.orchestrator.cache.model.Fingerprint
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
        val fingerprints = listOf(
            Fingerprint(
                IFingerIdentifier.RIGHT_THUMB,
                "template".toByteArray(),
                qualityScore = 3
            )
        )
        return FingerprintCaptureResponse(fingerprints)
    }

    private fun verifyResponses(originalResponse: FingerprintCaptureResponse,
                                processedResponse: FingerprintCaptureResponse) {
        processedResponse.fingerprints.forEachIndexed { index, processedFingerprint ->
            val originalTemplate = originalResponse.fingerprints[index].template
            val processedTemplate = processedFingerprint.template
            assertThat(processedTemplate, not(equalTo(originalTemplate)))
        }
    }

}
