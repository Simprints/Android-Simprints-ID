package com.simprints.fingerprint.data.domain.images

enum class SaveFingerprintImagesStrategy {
    NEVER,
    WSQ_15
}

fun SaveFingerprintImagesStrategy.deduceFileExtension(): String =
    when (this) {
        SaveFingerprintImagesStrategy.NEVER -> ""
        SaveFingerprintImagesStrategy.WSQ_15 -> "wsq"
    }
