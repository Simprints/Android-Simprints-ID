package com.simprints.id.domain.modality

import com.simprints.core.domain.modality.Modes

enum class Modality {
    FACE,
    FINGER;
}

fun Modality.toMode() =
    when(this){
        Modality.FACE -> Modes.FACE
        Modality.FINGER -> Modes.FINGERPRINT
    }
