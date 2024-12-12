package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models

enum class Un20MessageMajorType(
    val majorByte: Byte,
) {
    // 0x1_ : Versioning & OTA
    GET_UN20_EXTENDED_APP_VERSION(0x10),
    START_OTA(0x11),
    WRITE_OTA_CHUNK(0x12),
    VERIFY_OTA(0x13),

    // 0x2_ : Sensor commands
    CAPTURE_FINGERPRINT(0x21),
    GET_IMAGE_QUALITY_PREVIEW(0x22),
    SET_SCAN_LED_STATE(0x23),

    // 0x3_ : Template commands
    GET_SUPPORTED_TEMPLATE_TYPES(0x30),
    GET_TEMPLATE(0x31),

    // 0x4_ : Image commands
    GET_SUPPORTED_IMAGE_FORMATS(0x40),
    GET_IMAGE(0x41),
    GET_IMAGE_QUALITY(0x42),

    /**
     * Get Unprocessed Image
     *
     * Returns the raw image data from the sensor.
     */
    GET_UNPROCESSED_IMAGE(0x43),

    /**
     *
     * Returns the UN20 calibration file (sgdevun20a.cfg) which can be used by Secugen Android library
     * to correct for the optical distortion of raw image.
     */
    GET_IMAGE_DISTORTION_CONFIGURATION_MATRIX(0x44),
}
