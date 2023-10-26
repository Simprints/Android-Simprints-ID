package com.simprints.fingerprint.capture.extensions

import com.simprints.infra.config.store.models.Vero2Configuration


internal fun Vero2Configuration.ImageSavingStrategy.deduceFileExtension(): String =
    when (this) {
        Vero2Configuration.ImageSavingStrategy.NEVER -> ""
        Vero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN,
        Vero2Configuration.ImageSavingStrategy.EAGER -> "wsq"
    }

internal fun Vero2Configuration.ImageSavingStrategy.isImageTransferRequired(): Boolean =
    when (this) {
        Vero2Configuration.ImageSavingStrategy.NEVER -> false
        Vero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN,
        Vero2Configuration.ImageSavingStrategy.EAGER -> true
    }

internal fun Vero2Configuration.ImageSavingStrategy.isEager(): Boolean =
    when (this) {
        Vero2Configuration.ImageSavingStrategy.EAGER -> true
        Vero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN,
        Vero2Configuration.ImageSavingStrategy.NEVER -> false
    }
