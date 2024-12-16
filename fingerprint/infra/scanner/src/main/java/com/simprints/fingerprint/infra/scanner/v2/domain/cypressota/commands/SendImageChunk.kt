package com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.commands

import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaCommandType

class SendImageChunk(
    private val imageChunk: ByteArray,
) : CypressOtaCommand(CypressOtaCommandType.SEND_IMAGE_CHUNK) {
    override fun getDataBytes(): ByteArray = imageChunk
}
