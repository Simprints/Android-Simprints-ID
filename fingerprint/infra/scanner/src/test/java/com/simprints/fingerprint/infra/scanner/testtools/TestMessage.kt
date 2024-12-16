package com.simprints.fingerprint.infra.scanner.testtools

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.IncomingMainMessage
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.MessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.incoming.common.MessageParser
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.accumulators.PacketToMainMessageAccumulator
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.unsignedToInt
import java.nio.ByteOrder

class TestMessage(
    private val bytes: ByteArray,
) : IncomingMainMessage {
    override fun getBytes(): ByteArray = bytes
}

class TestMessageParser : MessageParser<TestMessage> {
    override fun parse(messageBytes: ByteArray): TestMessage = TestMessage(messageBytes)
}

object TestMessageProtocol : MessageProtocol {
    override val byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN
    override val headerSize: Int = 4
    override val headerIndices: IntRange = 0..3
    override val messageTypeIndicesInHeader: IntRange = 0..1
    override val lengthIndicesInHeader: IntRange = 2..3

    override fun getDataLengthFromHeader(header: ByteArray): Int = header.extract({ short }, lengthIndicesInHeader).unsignedToInt()
}

class TestMessageAccumulator(
    testMessageParser: TestMessageParser,
) : PacketToMainMessageAccumulator<TestMessage>(TestMessageProtocol, testMessageParser)

fun List<TestMessage>.lowerHexStrings() = map { it.getBytes() }.toHexStrings().stripWhiteSpaceToLowercase()
