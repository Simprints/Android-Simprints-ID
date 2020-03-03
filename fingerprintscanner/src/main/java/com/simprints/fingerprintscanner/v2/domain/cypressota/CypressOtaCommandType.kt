package com.simprints.fingerprintscanner.v2.domain.cypressota

enum class CypressOtaCommandType(val byte: Byte) {
    PREPARE_DOWNLOAD(0x11),
    DOWNLOAD(0x12),
    VERIFY_IMAGE(0x13),
    SEND_IMAGE_CHUNK(0x22),
}
