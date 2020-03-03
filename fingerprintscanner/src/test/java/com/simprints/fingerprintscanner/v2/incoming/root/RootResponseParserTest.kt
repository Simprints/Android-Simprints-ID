package com.simprints.fingerprintscanner.v2.incoming.root

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.v2.domain.root.responses.EnterMainModeResponse
import com.simprints.fingerprintscanner.v2.exceptions.parsing.InvalidMessageException
import com.simprints.fingerprintscanner.v2.tools.primitives.hexToByteArray
import com.simprints.testtools.common.syntax.assertThrows
import org.junit.Test

class RootResponseParserTest {

    @Test
    fun parseRootResponse_buildsMessageCorrectlyFromProtocol() {
        val messageParser = RootResponseParser()

        val rawBytes = "F4 10 00 00".hexToByteArray()
        val expectedResponse = EnterMainModeResponse()
        val actualResponse = messageParser.parse(rawBytes)

        assertThat(actualResponse).isInstanceOf(expectedResponse::class.java)
        assertThat((actualResponse as EnterMainModeResponse).getBytes()).isEqualTo(expectedResponse.getBytes())
    }

    @Test
    fun parseRootResponse_receivesMessageTooShort_throwsException() {
        val messageParser = RootResponseParser()

        val rawBytes = "F4".hexToByteArray()
        assertThrows<InvalidMessageException> { messageParser.parse(rawBytes) }
    }

    @Test
    fun parseRootResponse_invalidMessageType_throwsException() {
        val messageParser = RootResponseParser()

        val rawBytes = "F4 B9 00 00".hexToByteArray()
        assertThrows<InvalidMessageException> { messageParser.parse(rawBytes) }
    }
}
