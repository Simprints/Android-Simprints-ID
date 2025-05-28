package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.FaceConfiguration

@Keep
internal data class ApiFaceConfiguration(
    val allowedSDKs: List<BioSdk>,
    val rankOne: ApiFaceSdkConfiguration? = null,
    val simFace: ApiFaceSdkConfiguration? = null,
) {
    fun toDomain(): FaceConfiguration = FaceConfiguration(
        allowedSDKs = allowedSDKs.map { it.toDomain() },
        rankOne = rankOne?.toDomain(),
        simFace = simFace?.toDomain(),
    )

    @Keep
    data class ApiFaceSdkConfiguration(
        val nbOfImagesToCapture: Int,
        val qualityThreshold: Float,
        val decisionPolicy: ApiDecisionPolicy,
        val imageSavingStrategy: ImageSavingStrategy,
        val allowedAgeRange: ApiAllowedAgeRange?,
        val verificationMatchThreshold: Float?,
        val version: String,
    ) {
        fun toDomain() = FaceConfiguration.FaceSdkConfiguration(
            nbOfImagesToCapture = nbOfImagesToCapture,
            qualityThreshold = qualityThreshold,
            decisionPolicy = decisionPolicy.toDomain(),
            imageSavingStrategy = imageSavingStrategy.toDomain(),
            allowedAgeRange = allowedAgeRange?.toDomain() ?: AgeGroup(0, null),
            verificationMatchThreshold = verificationMatchThreshold,
            version = version,
        )
    }

    @Keep
    enum class BioSdk {
        RANK_ONE,
        SIM_FACE,
        ;

        fun toDomain() = when (this) {
            RANK_ONE -> FaceConfiguration.BioSdk.RANK_ONE
            SIM_FACE -> FaceConfiguration.BioSdk.SIM_FACE
        }
    }

    @Keep
    enum class ImageSavingStrategy {
        NEVER,
        ONLY_USED_IN_REFERENCE,
        ONLY_GOOD_SCAN,
        ;

        fun toDomain(): FaceConfiguration.ImageSavingStrategy = when (this) {
            NEVER -> FaceConfiguration.ImageSavingStrategy.NEVER
            ONLY_USED_IN_REFERENCE -> FaceConfiguration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE
            ONLY_GOOD_SCAN -> FaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN
        }
    }
}
