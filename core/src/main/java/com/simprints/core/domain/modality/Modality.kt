package com.simprints.core.domain.modality

enum class Modality {
    FACE,
    FINGER;
}

fun Modality.toMode() =
    when(this){
        Modality.FACE -> Modes.FACE
        Modality.FINGER -> Modes.FINGERPRINT
    }
