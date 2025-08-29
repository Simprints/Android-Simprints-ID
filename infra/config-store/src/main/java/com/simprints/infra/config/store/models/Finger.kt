package com.simprints.infra.config.store.models

import com.simprints.core.domain.sample.SampleIdentifier

enum class Finger {
    LEFT_THUMB,
    LEFT_INDEX_FINGER,
    LEFT_3RD_FINGER,
    LEFT_4TH_FINGER,
    LEFT_5TH_FINGER,
    RIGHT_THUMB,
    RIGHT_INDEX_FINGER,
    RIGHT_3RD_FINGER,
    RIGHT_4TH_FINGER,
    RIGHT_5TH_FINGER,
}

fun Finger.fromDomainToModuleApi() = when (this) {
    Finger.RIGHT_5TH_FINGER -> SampleIdentifier.RIGHT_5TH_FINGER
    Finger.RIGHT_4TH_FINGER -> SampleIdentifier.RIGHT_4TH_FINGER
    Finger.RIGHT_3RD_FINGER -> SampleIdentifier.RIGHT_3RD_FINGER
    Finger.RIGHT_INDEX_FINGER -> SampleIdentifier.RIGHT_INDEX_FINGER
    Finger.RIGHT_THUMB -> SampleIdentifier.RIGHT_THUMB
    Finger.LEFT_THUMB -> SampleIdentifier.LEFT_THUMB
    Finger.LEFT_INDEX_FINGER -> SampleIdentifier.LEFT_INDEX_FINGER
    Finger.LEFT_3RD_FINGER -> SampleIdentifier.LEFT_3RD_FINGER
    Finger.LEFT_4TH_FINGER -> SampleIdentifier.LEFT_4TH_FINGER
    Finger.LEFT_5TH_FINGER -> SampleIdentifier.LEFT_5TH_FINGER
}

fun SampleIdentifier.fromModuleApiToDomain(): Finger = when (this) {
    SampleIdentifier.RIGHT_5TH_FINGER -> Finger.RIGHT_5TH_FINGER
    SampleIdentifier.RIGHT_4TH_FINGER -> Finger.RIGHT_4TH_FINGER
    SampleIdentifier.RIGHT_3RD_FINGER -> Finger.RIGHT_3RD_FINGER
    SampleIdentifier.RIGHT_INDEX_FINGER -> Finger.RIGHT_INDEX_FINGER
    SampleIdentifier.RIGHT_THUMB -> Finger.RIGHT_THUMB
    SampleIdentifier.LEFT_THUMB -> Finger.LEFT_THUMB
    SampleIdentifier.LEFT_INDEX_FINGER -> Finger.LEFT_INDEX_FINGER
    SampleIdentifier.LEFT_3RD_FINGER -> Finger.LEFT_3RD_FINGER
    SampleIdentifier.LEFT_4TH_FINGER -> Finger.LEFT_4TH_FINGER
    SampleIdentifier.LEFT_5TH_FINGER -> Finger.LEFT_5TH_FINGER
    else -> throw IllegalArgumentException("Not a finger sample")
}
