package com.simprints.infra.config.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.domain.models.FaceConfiguration

@Keep
internal data class ApiFaceConfiguration(
    val nbOfImagesToCapture: Int,
    val qualityThreshold: Int,
    val imageSavingStrategy: ImageSavingStrategy,
    val decisionPolicy: ApiDecisionPolicy,
) {

    fun toDomain(): FaceConfiguration =
        FaceConfiguration(
            nbOfImagesToCapture,
            qualityThreshold,
            imageSavingStrategy.toDomain(),
            decisionPolicy.toDomain()
        )

    @Keep
    enum class ImageSavingStrategy {
        NEVER,
        ONLY_GOOD_SCAN;

        fun toDomain(): FaceConfiguration.ImageSavingStrategy =
            when (this) {
                NEVER -> FaceConfiguration.ImageSavingStrategy.NEVER
                ONLY_GOOD_SCAN -> FaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN
            }
    }
}
