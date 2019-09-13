package com.simprints.fingerprintscanner.v2.domain.message.vero.models

class FirmwareVersion(
    val majorVersionCode: Int,
    val minorVersionCode: Int,
    val magicBytes: ByteArray
)
