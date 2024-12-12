package com.simprints.fingerprint.infra.scanner.v2.domain.stmota

import com.simprints.fingerprint.infra.scanner.v2.domain.Protocol
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands.EraseMemoryAddressCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands.EraseMemoryStartCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands.GoAddressCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands.GoCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands.InitBootloaderCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands.WriteMemoryAddressCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands.WriteMemoryDataCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands.WriteMemoryStartCommand
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.byteArrayOf
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.nxorAll
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.xorAll
import java.nio.ByteOrder

object StmOtaMessageProtocol : Protocol {
    override val byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN

    fun buildMessageBytes(message: StmOtaMessage) = when (message) {
        is InitBootloaderCommand -> message.getDataBytes()
        is EraseMemoryStartCommand -> appendComplement(message.getDataBytes())
        is EraseMemoryAddressCommand -> appendCheckSum(message.getDataBytes())
        is WriteMemoryStartCommand -> appendComplement(message.getDataBytes())
        is WriteMemoryAddressCommand -> appendCheckSum(message.getDataBytes())
        is WriteMemoryDataCommand -> appendCheckSum(message.getDataBytes())
        is GoCommand -> appendComplement(message.getDataBytes())
        is GoAddressCommand -> appendCheckSum(message.getDataBytes())
        else -> message.getDataBytes()
    }

    private fun appendCheckSum(bytes: ByteArray): ByteArray = byteArrayOf(bytes, bytes.xorAll())

    private fun appendComplement(bytes: ByteArray): ByteArray = byteArrayOf(bytes, bytes.nxorAll())
}
