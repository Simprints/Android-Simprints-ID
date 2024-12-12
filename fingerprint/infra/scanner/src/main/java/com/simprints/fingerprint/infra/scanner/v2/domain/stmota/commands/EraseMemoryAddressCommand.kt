package com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands

import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.StmOtaCommand

class EraseMemoryAddressCommand(
    val address: ByteArray,
) : StmOtaCommand() {
    override fun getDataBytes(): ByteArray = address
}
