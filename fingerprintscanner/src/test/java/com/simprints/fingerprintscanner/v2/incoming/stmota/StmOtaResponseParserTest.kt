package com.simprints.fingerprintscanner.v2.incoming.stmota

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.v2.domain.stmota.responses.CommandAcknowledgement
import com.simprints.fingerprintscanner.v2.tools.primitives.hexToByteArray
import org.junit.Test

class StmOtaResponseParserTest {

    @Test
    fun parseStmOtaResponse_buildsMessageCorrectlyFromProtocol() {
        val messageParser = StmOtaResponseParser()

        val rawBytes = "79".hexToByteArray()
        val expectedResponse = CommandAcknowledgement(CommandAcknowledgement.Kind.ACK)
        val actualResponse = messageParser.parse(rawBytes)

        assertThat(actualResponse).isInstanceOf(expectedResponse::class.java)
        assertThat((actualResponse as CommandAcknowledgement).kind).isEqualTo(expectedResponse.kind)
    }
}
