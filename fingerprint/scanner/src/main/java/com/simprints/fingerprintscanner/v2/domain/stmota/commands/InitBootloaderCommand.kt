package com.simprints.fingerprintscanner.v2.domain.stmota.commands

import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaCommand

class InitBootloaderCommand : StmOtaCommand() {

    override fun getDataBytes(): ByteArray = byteArrayOf(INIT_BOOTLOADER_COMMAND_BYTE)

    companion object {
        const val INIT_BOOTLOADER_COMMAND_BYTE = 0x7F.toByte()
    }
}
