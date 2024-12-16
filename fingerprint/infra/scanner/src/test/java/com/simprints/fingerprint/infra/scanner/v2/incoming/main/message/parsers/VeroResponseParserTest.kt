package com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.parsers

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroMessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.StmExtendedFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetStmExtendedFirmwareVersionResponse
import org.junit.Test

class VeroResponseParserTest {
    val messageParser = VeroResponseParser()

    @Test
    fun shouldParse_stmExtendedVersionResponse_successfully() {
        // given
        val expectedResponse = GetStmExtendedFirmwareVersionResponse(
            StmExtendedFirmwareVersion("1.E-1.4"),
        )
        val payload = byteArrayOf(0x31, 0x2E, 0x45, 0x2D, 0x31, 0x2E, 0x34)
        val messageBytes = VeroMessageProtocol.buildMessageBytes(
            veroMessageType = VeroMessageType.GET_STM_EXTENDED_FIRMWARE_VERSION,
            data = payload,
        )

        // when
        val actualResponse = messageParser.parse(messageBytes)

        // then
        assertThat(actualResponse).isInstanceOf(GetStmExtendedFirmwareVersionResponse::class.java)
        assertThat(actualResponse).isEqualTo(expectedResponse)
    }
}
