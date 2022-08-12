package com.simprints.infra.config.domain.models

import androidx.annotation.Keep

data class Vero2Configuration(
    val imageSavingStrategy: ImageSavingStrategy,
    val captureStrategy: CaptureStrategy,
    val displayLiveFeedback: Boolean,
    val firmwareVersions: Map<String, Vero2FirmwareVersions>,
) {

    enum class ImageSavingStrategy {
        NEVER,
        ONLY_GOOD_SCAN,
        EAGER;
    }

    enum class CaptureStrategy {
        SECUGEN_ISO_500_DPI,
        SECUGEN_ISO_1000_DPI,
        SECUGEN_ISO_1300_DPI,
        SECUGEN_ISO_1700_DPI;
    }

    @Keep
    data class Vero2FirmwareVersions(
        val cypress: String,
        val stm: String,
        val un20: String,
    )
}
