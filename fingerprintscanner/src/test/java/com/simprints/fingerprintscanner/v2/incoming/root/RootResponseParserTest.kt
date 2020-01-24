package com.simprints.fingerprintscanner.v2.incoming.root

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.v2.domain.root.responses.EnterMainModeResponse
import com.simprints.fingerprintscanner.v2.tools.primitives.hexToByteArray
import org.junit.Test

class RootResponseParserTest {

    @Test
    fun parseRootResponse_buildsMessageCorrectlyFromProtocol() {
        val messageParser = RootResponseParser()

        val rawBytes = "F0 10 00 00".hexToByteArray()
        val expectedResponse = EnterMainModeResponse()
        val actualResponse = messageParser.parse(rawBytes)

        assertThat(actualResponse).isInstanceOf(expectedResponse::class.java)
        assertThat((actualResponse as EnterMainModeResponse).getBytes()).isEqualTo(expectedResponse.getBytes())
    }
}
