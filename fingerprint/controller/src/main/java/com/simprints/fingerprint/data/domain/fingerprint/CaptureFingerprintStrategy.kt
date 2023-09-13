package com.simprints.fingerprint.data.domain.fingerprint

/**
 * This enum represents the image quality of the fingerprint image to be captured
 */
import com.simprints.infra.config.domain.models.Vero2Configuration


fun Vero2Configuration.CaptureStrategy.toInt(): Int =
    when (this) {
        Vero2Configuration.CaptureStrategy.SECUGEN_ISO_500_DPI -> 500
        Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI -> 1000
        Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1300_DPI -> 1300
        Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1700_DPI -> 1700
    }
