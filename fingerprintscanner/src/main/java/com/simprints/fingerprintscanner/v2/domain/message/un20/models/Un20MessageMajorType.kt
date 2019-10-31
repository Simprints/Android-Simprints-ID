package com.simprints.fingerprintscanner.v2.domain.message.un20.models

enum class Un20MessageMajorType(val majorByte: Byte) {
    // 0x1_ : Versioning
    GET_UN20_APP_VERSION(0x10),

    // 0x2_ : Sensor commands
    CAPTURE_FINGERPRINT(0x21),
    GET_IMAGE_QUALITY(0x22),

    // 0x3_ : Template commands
    GET_SUPPORTED_TEMPLATE_TYPES(0x30),
    GET_TEMPLATE(0x31),

    // 0x4_ : Image commands
    GET_SUPPORTED_IMAGE_FORMATS(0x40),
    GET_IMAGE(0x41)
}
