package com.simprints.infra.config.store.models

data class FaceConfiguration(
    val allowedSDKs: List<BioSdk>,
    val rankOne: FaceSdkConfiguration?,
    val simFace: FaceSdkConfiguration?,
) {
    data class FaceSdkConfiguration(
        val nbOfImagesToCapture: Int,
        val qualityThreshold: Float,
        val imageSavingStrategy: ImageSavingStrategy,
        override val decisionPolicy: DecisionPolicy,
        val version: String,
        override val allowedAgeRange: AgeGroup = AgeGroup(0, null),
        override val verificationMatchThreshold: Float? = null,
    ) : ModalitySdkConfiguration

    fun getSdkConfiguration(sdk: BioSdk): FaceSdkConfiguration? = when (sdk) {
        BioSdk.RANK_ONE -> rankOne
        BioSdk.SIM_FACE -> simFace
    }

    enum class BioSdk : ModalitySdkType {
        RANK_ONE,
        SIM_FACE,
    }

    enum class ImageSavingStrategy {
        NEVER,
        ONLY_USED_IN_REFERENCE,
        ONLY_GOOD_SCAN,
        ;

        fun shouldSaveImage() = this != NEVER
    }
}
