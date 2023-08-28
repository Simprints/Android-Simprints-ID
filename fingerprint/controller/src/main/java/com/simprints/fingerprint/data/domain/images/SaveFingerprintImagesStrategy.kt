package com.simprints.fingerprint.data.domain.images

import com.simprints.infra.config.domain.models.Vero2Configuration


fun Vero2Configuration.ImageSavingStrategy.deduceFileExtension(): String =
    when (this) {
        Vero2Configuration.ImageSavingStrategy.NEVER -> ""
        Vero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN,
        Vero2Configuration.ImageSavingStrategy.EAGER -> "wsq"
    }

fun Vero2Configuration.ImageSavingStrategy.isImageTransferRequired(): Boolean =
    when (this) {
        Vero2Configuration.ImageSavingStrategy.NEVER -> false
        Vero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN,
        Vero2Configuration.ImageSavingStrategy.EAGER -> true
    }

fun Vero2Configuration.ImageSavingStrategy.isEager(): Boolean =
    when (this) {
        Vero2Configuration.ImageSavingStrategy.EAGER -> true
        Vero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN,
        Vero2Configuration.ImageSavingStrategy.NEVER -> false
    }
