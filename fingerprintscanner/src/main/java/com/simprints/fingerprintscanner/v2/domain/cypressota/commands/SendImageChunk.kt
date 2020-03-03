package com.simprints.fingerprintscanner.v2.domain.cypressota.commands

import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaCommand
import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaCommandType

class SendImageChunk(val imageChunk: ByteArray) : CypressOtaCommand(CypressOtaCommandType.SEND_IMAGE_CHUNK) {

    override fun getDataBytes(): ByteArray = imageChunk
}
