package com.simprints.infra.config.store.models

import androidx.annotation.Keep

@Keep
data class Vero2Configuration(
    val qualityThreshold: Int,
    val imageSavingStrategy: ImageSavingStrategy,
    val captureStrategy: CaptureStrategy,
    val ledsMode: LedsMode,
    val firmwareVersions: Map<String, Vero2FirmwareVersions>,
) {

    enum class ImageSavingStrategy {
        NEVER,
        ONLY_GOOD_SCAN,
        ONLY_USED_IN_REFERENCE,
        EAGER;
    }

    enum class CaptureStrategy {
        SECUGEN_ISO_500_DPI,
        SECUGEN_ISO_1000_DPI,
        SECUGEN_ISO_1300_DPI,
        SECUGEN_ISO_1700_DPI;
    }

    enum class LedsMode {
        BASIC,
        LIVE_QUALITY_FEEDBACK,
        VISUAL_SCAN_FEEDBACK;
    }

    @Keep
    data class Vero2FirmwareVersions(
        val cypress: String = "",
        val stm: String = "",
        val un20: String = "",
    )
}
