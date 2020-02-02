package com.simprints.fingerprintscanner.v2.domain.stmota.commands

import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaCommand

class EraseMemoryAddressCommand(val address: ByteArray) : StmOtaCommand() {

    override fun getDataBytes(): ByteArray = address
}
