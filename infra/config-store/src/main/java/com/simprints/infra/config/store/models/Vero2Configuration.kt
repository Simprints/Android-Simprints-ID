package com.simprints.infra.config.store.models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Vero2Configuration(
    val qualityThreshold: Int,
    val imageSavingStrategy: ImageSavingStrategy,
    val captureStrategy: CaptureStrategy,
    val ledsMode: LedsMode,
    val firmwareVersions: Map<String, Vero2FirmwareVersions>,
) {
    @Keep
    @Serializable
    enum class ImageSavingStrategy {
        NEVER,
        ONLY_GOOD_SCAN,
        ONLY_USED_IN_REFERENCE,
        EAGER,
    }

    @Keep
    @Serializable
    enum class CaptureStrategy {
        SECUGEN_ISO_500_DPI,
        SECUGEN_ISO_1000_DPI,
        SECUGEN_ISO_1300_DPI,
        SECUGEN_ISO_1700_DPI,
    }

    @Keep
    @Serializable
    enum class LedsMode {
        BASIC,
        LIVE_QUALITY_FEEDBACK,
        VISUAL_SCAN_FEEDBACK,
    }

    @Keep
    @Serializable
    data class Vero2FirmwareVersions(
        val cypress: String = "",
        val stm: String = "",
        val un20: String = "",
    )
}
