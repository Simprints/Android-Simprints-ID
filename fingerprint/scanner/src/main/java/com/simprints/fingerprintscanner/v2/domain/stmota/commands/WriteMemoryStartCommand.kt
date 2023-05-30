package com.simprints.fingerprintscanner.v2.domain.stmota.commands

import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaCommand

class WriteMemoryStartCommand : StmOtaCommand() {

    override fun getDataBytes(): ByteArray = byteArrayOf(WRITE_MEMORY_COMMAND_BYTE)

    companion object {
        const val WRITE_MEMORY_COMMAND_BYTE = 0x31.toByte()
    }
}
