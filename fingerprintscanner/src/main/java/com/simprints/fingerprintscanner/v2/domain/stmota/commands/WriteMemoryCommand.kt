package com.simprints.fingerprintscanner.v2.domain.stmota.commands

import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaCommand
import com.simprints.fingerprintscanner.v2.tools.primitives.byteArrayOf

class WriteMemoryStartCommand : StmOtaCommand() {

    override fun getDataBytes(): ByteArray = byteArrayOf(WRITE_MEMORY_COMMAND_BYTE)

    companion object {
        const val WRITE_MEMORY_COMMAND_BYTE = 0x31.toByte()
    }
}

class WriteMemoryAddressCommand(val address: ByteArray) : StmOtaCommand() {

    override fun getDataBytes(): ByteArray = address
}

class WriteMemoryDataCommand(val data: ByteArray) : StmOtaCommand() {

    override fun getDataBytes(): ByteArray = byteArrayOf(data.size.toByte(), data)
}
