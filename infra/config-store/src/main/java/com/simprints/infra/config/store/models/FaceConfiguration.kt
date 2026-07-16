package com.simprints.infra.config.store.models

import com.simprints.core.domain.common.AgeGroup

data class FaceConfiguration(
    val allowedSDKs: List<ModalitySdkType>,
    val isAutoCapture: Boolean,
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

    fun getSdkConfiguration(sdk: ModalitySdkType): FaceSdkConfiguration? = when (sdk) {
        ModalitySdkType.RANK_ONE -> rankOne
        ModalitySdkType.SIM_FACE -> simFace
        else -> null
    }

    enum class ImageSavingStrategy {
        NEVER,
        ONLY_USED_IN_REFERENCE,
        ONLY_GOOD_SCAN,
        ;

        fun shouldSaveImage() = this != NEVER
    }

    // TODO Will be a field in FaceSdkConfiguration later
    data class SpoofCheckConfiguration(
        val mode: SpoofCheckMode,
        val threshold: Float,
        val maxAttempts: Int,
        val maxBitmapSize: Int,
        val validationUiDurationMs: Int,
        val validationErrorUiDurationMs: Int,
    ) {
        companion object {
            val DISABLED = SpoofCheckConfiguration(
                mode = SpoofCheckMode.DISABLED,
                threshold = 0f,
                maxAttempts = 0,
                maxBitmapSize = 0,
                validationUiDurationMs = 0,
                validationErrorUiDurationMs = 0,
            )
        }
    }

    enum class SpoofCheckMode {
        DISABLED,
        ENFORCED,
        RECORDED,
        ;

        companion object {
            // TODO Later it will be enforced by API.
            fun fromString(mode: String): SpoofCheckMode = when (mode.uppercase()) {
                "ENFORCED" -> ENFORCED
                "RECORDED" -> RECORDED
                else -> DISABLED
            }
        }
    }
}
