package com.simprints.id.domain.moduleapi.fingerprint.models

import com.simprints.infra.config.domain.models.Finger
import com.simprints.moduleapi.fingerprint.IFingerIdentifier

fun Finger.fromDomainToModuleApi() = when (this) {
    Finger.RIGHT_5TH_FINGER -> IFingerIdentifier.RIGHT_5TH_FINGER
    Finger.RIGHT_4TH_FINGER -> IFingerIdentifier.RIGHT_4TH_FINGER
    Finger.RIGHT_3RD_FINGER -> IFingerIdentifier.RIGHT_3RD_FINGER
    Finger.RIGHT_INDEX_FINGER -> IFingerIdentifier.RIGHT_INDEX_FINGER
    Finger.RIGHT_THUMB -> IFingerIdentifier.RIGHT_THUMB
    Finger.LEFT_THUMB -> IFingerIdentifier.LEFT_THUMB
    Finger.LEFT_INDEX_FINGER -> IFingerIdentifier.LEFT_INDEX_FINGER
    Finger.LEFT_3RD_FINGER -> IFingerIdentifier.LEFT_3RD_FINGER
    Finger.LEFT_4TH_FINGER -> IFingerIdentifier.LEFT_4TH_FINGER
    Finger.LEFT_5TH_FINGER -> IFingerIdentifier.LEFT_5TH_FINGER
}


fun IFingerIdentifier.fromModuleApiToDomain(): Finger = when (this) {
    IFingerIdentifier.RIGHT_5TH_FINGER -> Finger.RIGHT_5TH_FINGER
    IFingerIdentifier.RIGHT_4TH_FINGER -> Finger.RIGHT_4TH_FINGER
    IFingerIdentifier.RIGHT_3RD_FINGER -> Finger.RIGHT_3RD_FINGER
    IFingerIdentifier.RIGHT_INDEX_FINGER -> Finger.RIGHT_INDEX_FINGER
    IFingerIdentifier.RIGHT_THUMB -> Finger.RIGHT_THUMB
    IFingerIdentifier.LEFT_THUMB -> Finger.LEFT_THUMB
    IFingerIdentifier.LEFT_INDEX_FINGER -> Finger.LEFT_INDEX_FINGER
    IFingerIdentifier.LEFT_3RD_FINGER -> Finger.LEFT_3RD_FINGER
    IFingerIdentifier.LEFT_4TH_FINGER -> Finger.LEFT_4TH_FINGER
    IFingerIdentifier.LEFT_5TH_FINGER -> Finger.LEFT_5TH_FINGER
}
