package com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.commands

import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaCommandType
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaMessageProtocol

class DownloadCommand(
    private val imageSize: Int,
) : CypressOtaCommand(CypressOtaCommandType.DOWNLOAD) {
    override fun getDataBytes(): ByteArray = with(CypressOtaMessageProtocol) {
        imageSize.toByteArray()
    }
}
