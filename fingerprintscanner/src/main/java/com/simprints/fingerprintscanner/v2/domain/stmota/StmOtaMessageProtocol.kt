package com.simprints.fingerprintscanner.v2.domain.stmota

import com.simprints.fingerprintscanner.v2.domain.Protocol
import com.simprints.fingerprintscanner.v2.domain.stmota.commands.InitBootloaderCommand
import com.simprints.fingerprintscanner.v2.domain.stmota.commands.WriteMemoryAddressCommand
import com.simprints.fingerprintscanner.v2.domain.stmota.commands.WriteMemoryDataCommand
import com.simprints.fingerprintscanner.v2.domain.stmota.commands.WriteMemoryStartCommand
import com.simprints.fingerprintscanner.v2.tools.primitives.byteArrayOf
import com.simprints.fingerprintscanner.v2.tools.primitives.nxorAll
import com.simprints.fingerprintscanner.v2.tools.primitives.xorAll
import java.nio.ByteOrder

object StmOtaMessageProtocol : Protocol {
    override val byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN

    fun buildMessageBytes(message: StmOtaMessage) =
        when (message) {
            is WriteMemoryStartCommand -> appendComplement(message.getDataBytes())
            is WriteMemoryAddressCommand -> appendCheckSum(message.getDataBytes())
            is WriteMemoryDataCommand -> appendCheckSum(message.getDataBytes())
            is InitBootloaderCommand -> message.getDataBytes()
            else -> message.getDataBytes()
        }

    private fun appendCheckSum(bytes: ByteArray): ByteArray = byteArrayOf(bytes, bytes.xorAll())

    private fun appendComplement(bytes: ByteArray): ByteArray = byteArrayOf(bytes, bytes.nxorAll())
}
