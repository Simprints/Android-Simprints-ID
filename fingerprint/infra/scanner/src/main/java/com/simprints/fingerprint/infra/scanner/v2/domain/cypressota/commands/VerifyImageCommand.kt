package com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.commands

import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaCommandType
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaMessageProtocol

class VerifyImageCommand(
    private val crc32: Int,
) : CypressOtaCommand(CypressOtaCommandType.VERIFY_IMAGE) {
    override fun getDataBytes(): ByteArray = with(CypressOtaMessageProtocol) {
        crc32.toByteArray()
    }
}
