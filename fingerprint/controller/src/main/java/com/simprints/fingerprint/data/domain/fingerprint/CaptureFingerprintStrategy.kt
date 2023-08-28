package com.simprints.fingerprint.data.domain.fingerprint

/**
 * This enum represents the image quality of the fingerprint image to be captured
 */
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi
import com.simprints.infra.config.domain.models.Vero2Configuration



fun Vero2Configuration.CaptureStrategy.toDomain(): Dpi =
    when (this) {
        Vero2Configuration.CaptureStrategy.SECUGEN_ISO_500_DPI -> Dpi(500)
        Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI -> Dpi(1000)
        Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1300_DPI -> Dpi(1300)
        Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1700_DPI -> Dpi(1700)
    }
