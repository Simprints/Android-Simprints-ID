package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.Vero2Configuration

@Keep
internal data class ApiVero2Configuration(
    val qualityThreshold: Int,
    val imageSavingStrategy: ImageSavingStrategy,
    val captureStrategy: CaptureStrategy,
    val ledsMode: LedsMode,
    val firmwareVersions: Map<String, ApiVero2FirmwareVersions>,
) {

    fun toDomain(): Vero2Configuration =
        Vero2Configuration(
            qualityThreshold,
            imageSavingStrategy.toDomain(),
            captureStrategy.toDomain(),
            ledsMode.toDomain(),
            firmwareVersions.mapValues { it.value.toDomain() }
        )

    @Keep
    enum class ImageSavingStrategy {
        NEVER,
        ONLY_GOOD_SCAN,
        ONLY_USED_IN_REFERENCE,
        EAGER;

        fun toDomain(): Vero2Configuration.ImageSavingStrategy =
            when (this) {
                NEVER -> Vero2Configuration.ImageSavingStrategy.NEVER
                ONLY_USED_IN_REFERENCE -> Vero2Configuration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE
                ONLY_GOOD_SCAN -> Vero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN
                EAGER -> Vero2Configuration.ImageSavingStrategy.EAGER
            }
    }

    @Keep
    enum class CaptureStrategy {
        SECUGEN_ISO_500_DPI,
        SECUGEN_ISO_1000_DPI,
        SECUGEN_ISO_1300_DPI,
        SECUGEN_ISO_1700_DPI;

        fun toDomain(): Vero2Configuration.CaptureStrategy =
            when (this) {
                SECUGEN_ISO_500_DPI -> Vero2Configuration.CaptureStrategy.SECUGEN_ISO_500_DPI
                SECUGEN_ISO_1000_DPI -> Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI
                SECUGEN_ISO_1300_DPI -> Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1300_DPI
                SECUGEN_ISO_1700_DPI -> Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1700_DPI
            }
    }

    @Keep
    enum class LedsMode {
        BASIC,
        LIVE_QUALITY_FEEDBACK,
        VISUAL_SCAN_FEEDBACK;

        fun toDomain(): Vero2Configuration.LedsMode =
            when (this) {
                BASIC -> Vero2Configuration.LedsMode.BASIC
                LIVE_QUALITY_FEEDBACK -> Vero2Configuration.LedsMode.LIVE_QUALITY_FEEDBACK
                VISUAL_SCAN_FEEDBACK -> Vero2Configuration.LedsMode.VISUAL_SCAN_FEEDBACK
            }
    }
    @Keep
    data class ApiVero2FirmwareVersions(
        val cypress: String,
        val stm: String,
        val un20: String,
    ) {
        fun toDomain(): Vero2Configuration.Vero2FirmwareVersions =
            Vero2Configuration.Vero2FirmwareVersions(cypress, stm, un20)
    }
}
