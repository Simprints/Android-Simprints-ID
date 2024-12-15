package com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.parsers

import com.google.common.truth.Truth
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20MessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20ExtendedAppVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20MessageType
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetUn20ExtendedAppVersionResponse
import org.junit.Test

class Un20ResponseParserTest {
    private val messageParser = Un20ResponseParser()

    @Test
    fun shouldParse_un20ExtendedAppVersionResponse_successfully() {
        // given
        val expectedResponse = GetUn20ExtendedAppVersionResponse(
            Un20ExtendedAppVersion("1.E-1.0"),
        )
        val payload = byteArrayOf(0x31, 0x2E, 0x45, 0x2D, 0x31, 0x2E, 0x30)
        val messageBytes = Un20MessageProtocol.buildMessageBytes(
            un20MessageType = Un20MessageType.GetUn20ExtendedAppVersion,
            data = payload,
        )

        // when
        val actualResponse = messageParser.parse(messageBytes)

        // then
        Truth.assertThat(actualResponse).isInstanceOf(GetUn20ExtendedAppVersionResponse::class.java)
        Truth.assertThat(actualResponse).isEqualTo(expectedResponse)
    }
}
