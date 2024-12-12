package com.simprints.fingerprint.infra.scanner.v2.incoming.root

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20ExtendedAppVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.StmExtendedFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.CypressExtendedFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.ExtendedVersionInformation
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.EnterMainModeResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetCypressExtendedVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetExtendedVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.exceptions.parsing.InvalidMessageException
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.hexToByteArray
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

    @Test
    fun shouldParse_cypressExtendedVersion_correctly() {
        val expectedResponse = CypressExtendedFirmwareVersion("1.E-1.1")
        val payloadBytes = byteArrayOf(0x31, 0x2E, 0x45, 0x2D, 0x31, 0x2E, 0x31)
        val messageRawBytes = RootMessageProtocol.buildMessageBytes(
            // message type
            RootMessageType.GET_CYPRESS_EXTENDED_VERSION,
            payloadBytes,
        )

        val messageParser = RootResponseParser()
        val actualResponse = messageParser.parse(messageRawBytes)

        assertThat(actualResponse).isInstanceOf(GetCypressExtendedVersionResponse::class.java)
        assertThat((actualResponse as GetCypressExtendedVersionResponse).version).isEqualTo(expectedResponse)
    }

    @Test
    fun shouldParse_extendedVersionInfo_correctly() {
        val expectedResponse = ExtendedVersionInformation(
            cypressFirmwareVersion = CypressExtendedFirmwareVersion("1.E-1.1"),
            stmFirmwareVersion = StmExtendedFirmwareVersion("1.E-1.4"),
            un20AppVersion = Un20ExtendedAppVersion("1.E-1.0"),
        )

        // extended version info
        val payloadRawBytes = byteArrayOf(
            0x07, // stm data length
            0x31,
            0x2E,
            0x45,
            0x2D,
            0x31,
            0x2E,
            0x31, // stm data
            0x07, // cypress data length
            0x31,
            0x2E,
            0x45,
            0x2D,
            0x31,
            0x2E,
            0x34, // cypress data
            0x07, // un20 data length
            0x31,
            0x2E,
            0x45,
            0x2D,
            0x31,
            0x2E,
            0x30, // un20 data
        )

        val messageRawBytes = RootMessageProtocol.buildMessageBytes(
            // message type
            RootMessageType.GET_EXTENDED_VERSION,
            // message body
            payloadRawBytes,
        )

        val messageParser = RootResponseParser()
        val actualResponse = messageParser.parse(messageRawBytes)

        assertThat(actualResponse).isInstanceOf(GetExtendedVersionResponse::class.java)
        assertThat((actualResponse as GetExtendedVersionResponse).version).isEqualTo(expectedResponse)
    }
}
