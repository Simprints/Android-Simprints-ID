package com.simprints.id.orchestrator.cache.crypto

import androidx.test.platform.app.InstrumentationRegistry
import com.simprints.core.images.SecuredImageRef
import com.simprints.id.data.secure.keystore.KeystoreManagerImpl
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceSample
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.util.*

class FaceCaptureResponseProcessorAndroidTest {

    private val processor by lazy {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val keystoreManager = KeystoreManagerImpl(context)
        FaceCaptureResponseProcessor(keystoreManager)
    }

    @Test
    fun shouldEncryptTemplates() {
        val originalResponse = mockResponse()
        val processedResponse = processor.process(originalResponse, Operation.ENCODE)

        require(processedResponse is FaceCaptureResponse)

        verifyResponses(originalResponse, processedResponse)
    }

    @Test
    fun shouldDecryptTemplates() {
        val originalResponse = processor.process(mockResponse(), Operation.ENCODE)
        val processedResponse = processor.process(originalResponse, Operation.DECODE)

        require(originalResponse is FaceCaptureResponse)
        require(processedResponse is FaceCaptureResponse)

        verifyResponses(originalResponse, processedResponse)
    }

    private fun mockResponse() = FaceCaptureResponse(listOf(
        FaceCaptureResult(
            index = 0,
            result = FaceSample(
                "face_id_0",
                UUID.randomUUID().toString().toByteArray(),
                SecuredImageRef("uri_0")
            )
        ),
        FaceCaptureResult(
            index = 2,
            result = FaceSample(
                "face_id_1",
                UUID.randomUUID().toString().toByteArray(),
                SecuredImageRef("uri_1")
            )
        )
    ))

    private fun verifyResponses(originalResponse: FaceCaptureResponse,
                                processedResponse: FaceCaptureResponse) {
        processedResponse.capturingResult.forEachIndexed { index, it ->
            it.result?.template?.let { template ->
                val originalTemplate = originalResponse.capturingResult[index].result?.template
                assertThat(template, not(equalTo(originalTemplate)))
            }
        }
    }

}
