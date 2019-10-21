package com.simprints.id.orchestrator.cache.crypto.response

import androidx.test.platform.app.InstrumentationRegistry
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.id.secure.cryptography.HybridCipherImpl
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test
import java.util.*

class FingerprintEnrolResponseEncoderAndroidTest {

    private val encoder by lazy {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val cipher = HybridCipherImpl(context)
        FingerprintEnrolResponseEncoder(cipher)
    }

    @Test
    fun shouldEncryptGuid() {
        val originalResponse = mockResponse()
        val processedResponse = encoder.process(originalResponse, Operation.ENCODE)

        require(processedResponse is FingerprintEnrolResponse)
        MatcherAssert.assertThat(processedResponse.guid, CoreMatchers.not(CoreMatchers.equalTo(originalResponse.guid)))
    }

    @Test
    fun shouldDecryptGuid() {
        val originalResponse = encoder.process(mockResponse(), Operation.ENCODE)
        val processedResponse = encoder.process(originalResponse, Operation.DECODE)

        require(originalResponse is FingerprintEnrolResponse)
        require(processedResponse is FingerprintEnrolResponse)
        MatcherAssert.assertThat(processedResponse.guid, CoreMatchers.not(CoreMatchers.equalTo(originalResponse.guid)))
    }

    private fun mockResponse() = FingerprintEnrolResponse(UUID.randomUUID().toString())

}
