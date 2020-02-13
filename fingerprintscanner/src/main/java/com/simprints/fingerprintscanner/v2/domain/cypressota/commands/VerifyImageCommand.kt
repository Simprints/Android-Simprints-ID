package com.simprints.fingerprintscanner.v2.domain.cypressota.commands

import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaCommand
import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaCommandType
import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaMessageProtocol

class VerifyImageCommand(val crc32: Int) : CypressOtaCommand(CypressOtaCommandType.VERIFY_IMAGE) {

    override fun getDataBytes(): ByteArray =
        with(CypressOtaMessageProtocol) {
            crc32.toByteArray()
        }
}
