package com.simprints.fingerprint.infra.scanner.v2.incoming.cypressota

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.responses.ContinueResponse
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.hexToByteArray
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CypressOtaMessageInputStreamTest {
    private val cypressOtaResponseParser = CypressOtaResponseParser()
    private val cypressOtaMessageInputStream =
        CypressOtaMessageInputStream(cypressOtaResponseParser)

    @Test
    fun cypressOtaMessageInputStream_receiveCypressOtaResponse_correctlyForwardsResponse() = runTest {
        val messageBytes = "39".hexToByteArray()
        val expectedResponse = ContinueResponse()
        val inputStreamFlow = MutableSharedFlow<ByteArray>()
        cypressOtaMessageInputStream.connect(inputStreamFlow)
        launch {
            inputStreamFlow.emit(messageBytes)
        }
        val response = cypressOtaMessageInputStream.receiveResponse<CypressOtaResponse>()
        assertThat(response).isInstanceOf(expectedResponse::class.java)
    }
}
