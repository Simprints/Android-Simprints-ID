package com.simprints.fingerprintscanner.v2.domain.stmota.commands

import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaCommand

class WriteMemoryDataCommand(val data: ByteArray) : StmOtaCommand() {

    override fun getDataBytes(): ByteArray = com.simprints.fingerprintscanner.v2.tools.primitives.byteArrayOf(data.size.toByte(), data)
}
