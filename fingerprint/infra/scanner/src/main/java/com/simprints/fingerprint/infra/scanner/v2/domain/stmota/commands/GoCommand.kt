package com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands

import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.StmOtaCommand

class GoCommand : StmOtaCommand() {
    override fun getDataBytes(): ByteArray = byteArrayOf(GO_COMMAND_BYTE)

    companion object {
        const val GO_COMMAND_BYTE = 0x21.toByte()
    }
}
