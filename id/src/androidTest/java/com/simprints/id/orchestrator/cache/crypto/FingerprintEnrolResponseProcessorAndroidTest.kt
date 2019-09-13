package com.simprints.id.orchestrator.cache.crypto

import androidx.test.platform.app.InstrumentationRegistry
import com.simprints.id.data.secure.keystore.KeystoreManagerImpl
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test
import java.util.*

class FingerprintEnrolResponseProcessorAndroidTest {

    private val processor by lazy {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val keystoreManager = KeystoreManagerImpl(context)
        FingerprintEnrolResponseProcessor(keystoreManager)
    }

    @Test
    fun shouldEncryptGuid() {
        val originalResponse = mockResponse()
        val processedResponse = processor.process(originalResponse, Operation.ENCODE)

        require(processedResponse is FingerprintEnrolResponse)
        MatcherAssert.assertThat(processedResponse.guid, CoreMatchers.not(CoreMatchers.equalTo(originalResponse.guid)))
    }

    @Test
    fun shouldDecryptGuid() {
        val originalResponse = processor.process(mockResponse(), Operation.ENCODE)
        val processedResponse = processor.process(originalResponse, Operation.DECODE)

        require(originalResponse is FingerprintEnrolResponse)
        require(processedResponse is FingerprintEnrolResponse)
        MatcherAssert.assertThat(processedResponse.guid, CoreMatchers.not(CoreMatchers.equalTo(originalResponse.guid)))
    }

    private fun mockResponse() = FingerprintEnrolResponse(UUID.randomUUID().toString())

}
