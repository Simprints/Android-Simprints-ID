package com.simprints.fingerprintscanner.v2.domain.cypressota.commands

import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaCommand
import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaCommandType
import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaMessageProtocol

class DownloadCommand(val imageSize: Int) : CypressOtaCommand(CypressOtaCommandType.DOWNLOAD) {

    override fun getDataBytes(): ByteArray =
        with(CypressOtaMessageProtocol) {
            imageSize.toByteArray()
        }
}
