package com.simprints.fingerprintscanner.testtools

import com.simprints.fingerprintscanner.v2.domain.main.message.IncomingMainMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.MessageProtocol
import com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators.PacketToMainMessageAccumulator
import com.simprints.fingerprintscanner.v2.incoming.MessageParser
import com.simprints.fingerprintscanner.v2.tools.primitives.unsignedToInt
import java.nio.ByteOrder

class TestMessage(private val bytes: ByteArray) : IncomingMainMessage {

    override fun getBytes(): ByteArray = bytes
}

class TestMessageParser : MessageParser<TestMessage> {

    override fun parse(messageBytes: ByteArray): TestMessage = TestMessage(messageBytes)
}

object TestMessageProtocol : MessageProtocol {

    override val byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN
    override val HEADER_SIZE: Int = 4
    override val HEADER_INDICES: IntRange = 0..3
    override val MESSAGE_TYPE_INDICES_IN_HEADER: IntRange = 0..1
    override val LENGTH_INDICES_IN_HEADER: IntRange = 2..3

    override fun getDataLengthFromHeader(header: ByteArray): Int =
        header.extract({ short },
            LENGTH_INDICES_IN_HEADER
        ).unsignedToInt()
}

class TestMessageAccumulator(testMessageParser: TestMessageParser) :
    PacketToMainMessageAccumulator<TestMessage>(TestMessageProtocol, testMessageParser)

fun List<TestMessage>.lowerHexStrings() =
    map { it.getBytes() }.toHexStrings().stripWhiteSpaceToLowercase()
