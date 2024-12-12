package com.simprints.fingerprint.capture.extensions

import com.simprints.infra.config.store.models.Vero2Configuration.ImageSavingStrategy

internal fun ImageSavingStrategy.deduceFileExtension(): String = when (this) {
    ImageSavingStrategy.NEVER -> ""
    ImageSavingStrategy.ONLY_USED_IN_REFERENCE,
    ImageSavingStrategy.ONLY_GOOD_SCAN,
    ImageSavingStrategy.EAGER,
    -> "wsq"
}

internal fun ImageSavingStrategy.isImageTransferRequired(): Boolean = this != ImageSavingStrategy.NEVER

internal fun ImageSavingStrategy.isEager(): Boolean = this == ImageSavingStrategy.EAGER
