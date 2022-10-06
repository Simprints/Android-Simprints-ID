package com.simprints.infra.config.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.domain.models.Vero2Configuration

@Keep
internal data class ApiVero2Configuration(
    val imageSavingStrategy: ImageSavingStrategy,
    val captureStrategy: CaptureStrategy,
    val displayLiveFeedback: Boolean,
    val firmwareVersions: Map<String, ApiVero2FirmwareVersions>,
) {

    fun toDomain(): Vero2Configuration =
        Vero2Configuration(
            imageSavingStrategy.toDomain(),
            captureStrategy.toDomain(),
            displayLiveFeedback,
            firmwareVersions.mapValues { it.value.toDomain() }
        )

    @Keep
    enum class ImageSavingStrategy {
        NEVER,
        ONLY_GOOD_SCAN,
        EAGER;

        fun toDomain(): Vero2Configuration.ImageSavingStrategy =
            when (this) {
                NEVER -> Vero2Configuration.ImageSavingStrategy.NEVER
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
    data class ApiVero2FirmwareVersions(
        val cypress: String,
        val stm: String,
        val un20: String,
    ) {
        fun toDomain(): Vero2Configuration.Vero2FirmwareVersions =
            Vero2Configuration.Vero2FirmwareVersions(cypress, stm, un20)
    }
}
