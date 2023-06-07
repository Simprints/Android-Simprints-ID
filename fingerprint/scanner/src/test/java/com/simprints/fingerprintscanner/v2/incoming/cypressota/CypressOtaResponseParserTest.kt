package com.simprints.fingerprintscanner.v2.incoming.cypressota

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.v2.domain.cypressota.responses.ErrorResponse
import com.simprints.fingerprintscanner.v2.domain.cypressota.responses.OkResponse
import com.simprints.fingerprintscanner.v2.tools.primitives.hexToByteArray
import org.junit.Test

class CypressOtaResponseParserTest {

    @Test
    fun parseCypressOtaResponse_buildsMessageCorrectlyFromProtocol() {
        val messageParser = CypressOtaResponseParser()

        val rawBytes = "30".hexToByteArray()
        val expectedResponse = OkResponse()
        val actualResponse = messageParser.parse(rawBytes)

        assertThat(actualResponse).isInstanceOf(expectedResponse::class.java)
    }

    @Test
    fun parseCypressOtaResponse_receivesInvalidBytes_parsesAsErrorResponse() {
        val messageParser = CypressOtaResponseParser()

        val rawBytes = "FD F0".hexToByteArray()
        val expectedResponse = ErrorResponse()
        val actualResponse = messageParser.parse(rawBytes)

        assertThat(actualResponse).isInstanceOf(expectedResponse::class.java)
    }
}
