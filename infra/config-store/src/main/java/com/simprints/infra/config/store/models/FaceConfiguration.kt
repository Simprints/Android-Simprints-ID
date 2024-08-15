package com.simprints.infra.config.store.models

data class FaceConfiguration(
    val allowedSDKs: List<BioSdk>,
    val rankOne: FaceSdkConfiguration?,
) {

    val nbOfImagesToCapture: Int
        get() = rankOne?.nbOfImagesToCapture!!

    val qualityThreshold: Int
        get() = rankOne?.qualityThreshold!!

    val imageSavingStrategy: ImageSavingStrategy
        get() = rankOne?.imageSavingStrategy!!

    val decisionPolicy: DecisionPolicy
        get() = rankOne?.decisionPolicy!!

    val verificationMatchThreshold: Float?
        get() = rankOne?.verificationMatchThreshold

    data class FaceSdkConfiguration(
        val nbOfImagesToCapture: Int,
        val qualityThreshold: Int,
        val imageSavingStrategy: ImageSavingStrategy,
        val decisionPolicy: DecisionPolicy,
        val version: String,
        val allowedAgeRange: AgeGroup? = null,
        val verificationMatchThreshold: Float? = null,
    )

    enum class BioSdk {
        RANK_ONE,
    }

    enum class ImageSavingStrategy {
        NEVER,
        ONLY_USED_IN_REFERENCE,
        ONLY_GOOD_SCAN;

        fun shouldSaveImage() = this != NEVER
    }
}
