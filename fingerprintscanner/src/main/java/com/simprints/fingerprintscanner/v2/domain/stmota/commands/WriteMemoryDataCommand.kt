package com.simprints.fingerprintscanner.v2.domain.stmota.commands

import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaCommand
import com.simprints.fingerprintscanner.v2.tools.primitives.byteArrayOf

class WriteMemoryDataCommand(val data: ByteArray) : StmOtaCommand() {

    override fun getDataBytes(): ByteArray = byteArrayOf(data.size.toByte(), data)
}
