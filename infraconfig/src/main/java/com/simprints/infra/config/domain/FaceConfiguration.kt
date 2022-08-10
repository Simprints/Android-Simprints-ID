package com.simprints.infra.config.domain

data class FaceConfiguration(
    val nbOfImagesToCapture: Int,
    val qualityThreshold: Int,
    val imageSavingStrategy: ImageSavingStrategy,
    val decisionPolicy: DecisionPolicy,
) {

    enum class ImageSavingStrategy {
        NEVER,
        ONLY_GOOD_SCAN;
    }
}
