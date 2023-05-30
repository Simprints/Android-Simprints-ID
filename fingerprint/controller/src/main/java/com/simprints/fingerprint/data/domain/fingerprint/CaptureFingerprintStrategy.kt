package com.simprints.fingerprint.data.domain.fingerprint

/**
 * This enum represents the image quality of the fingerprint image to be captured
 */
import com.simprints.infra.config.domain.models.Vero2Configuration

enum class CaptureFingerprintStrategy {
    SECUGEN_ISO_500_DPI,
    SECUGEN_ISO_1000_DPI,
    SECUGEN_ISO_1300_DPI,
    SECUGEN_ISO_1700_DPI
}

fun Vero2Configuration.CaptureStrategy.toDomain(): CaptureFingerprintStrategy =
    when (this) {
        Vero2Configuration.CaptureStrategy.SECUGEN_ISO_500_DPI -> CaptureFingerprintStrategy.SECUGEN_ISO_500_DPI
        Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI -> CaptureFingerprintStrategy.SECUGEN_ISO_1000_DPI
        Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1300_DPI -> CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI
        Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1700_DPI -> CaptureFingerprintStrategy.SECUGEN_ISO_1700_DPI
    }
