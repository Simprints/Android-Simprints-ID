package com.simprints.fingerprint.data.domain.images

enum class SaveFingerprintImagesStrategy {
    NEVER,          // Never save fingerprint images
    WSQ_15,         // Save enrolled images using WSQ with 15x compression
    WSQ_15_EAGER    // Save ALL captured images using WSQ with 15x compression
}

fun SaveFingerprintImagesStrategy.deduceFileExtension(): String =
    when (this) {
        SaveFingerprintImagesStrategy.NEVER -> ""
        SaveFingerprintImagesStrategy.WSQ_15,
        SaveFingerprintImagesStrategy.WSQ_15_EAGER -> "wsq"
    }

fun SaveFingerprintImagesStrategy.isImageTransferRequired(): Boolean =
    when (this) {
        SaveFingerprintImagesStrategy.NEVER -> false
        SaveFingerprintImagesStrategy.WSQ_15,
        SaveFingerprintImagesStrategy.WSQ_15_EAGER -> true
    }

fun SaveFingerprintImagesStrategy.isEager(): Boolean =
    when (this) {
        SaveFingerprintImagesStrategy.WSQ_15_EAGER -> true
        SaveFingerprintImagesStrategy.WSQ_15,
        SaveFingerprintImagesStrategy.NEVER -> false
    }
