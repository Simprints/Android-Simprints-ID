package com.simprints.fingerprint.activities.collect.models

import com.simprints.moduleapi.fingerprint.requests.IFingerIdentifier
import com.simprints.id.FingerIdentifier as FingerIdentifierCore

enum class FingerIdentifier {
    RIGHT_5TH_FINGER,
    RIGHT_4TH_FINGER,
    RIGHT_3RD_FINGER,
    RIGHT_INDEX_FINGER,
    RIGHT_THUMB,
    LEFT_THUMB,
    LEFT_INDEX_FINGER,
    LEFT_3RD_FINGER,
    LEFT_4TH_FINGER,
    LEFT_5TH_FINGER
}

fun IFingerIdentifier.toDomainClass(): FingerIdentifier =
    when(this) {
        IFingerIdentifier.RIGHT_5TH_FINGER -> FingerIdentifier.RIGHT_5TH_FINGER
        IFingerIdentifier.RIGHT_4TH_FINGER -> FingerIdentifier.RIGHT_4TH_FINGER
        IFingerIdentifier.RIGHT_3RD_FINGER -> FingerIdentifier.RIGHT_3RD_FINGER
        IFingerIdentifier.RIGHT_INDEX_FINGER -> FingerIdentifier.RIGHT_INDEX_FINGER
        IFingerIdentifier.RIGHT_THUMB -> FingerIdentifier.RIGHT_THUMB
        IFingerIdentifier.LEFT_THUMB -> FingerIdentifier.LEFT_THUMB
        IFingerIdentifier.LEFT_INDEX_FINGER -> FingerIdentifier.LEFT_INDEX_FINGER
        IFingerIdentifier.LEFT_3RD_FINGER -> FingerIdentifier.LEFT_3RD_FINGER
        IFingerIdentifier.LEFT_4TH_FINGER -> FingerIdentifier.LEFT_4TH_FINGER
        IFingerIdentifier.LEFT_5TH_FINGER -> FingerIdentifier.LEFT_5TH_FINGER
    }

fun FingerIdentifier.fromDomainToCore(): FingerIdentifierCore =
    when(this) {
        FingerIdentifier.RIGHT_5TH_FINGER -> FingerIdentifierCore.RIGHT_5TH_FINGER
        FingerIdentifier.RIGHT_4TH_FINGER -> FingerIdentifierCore.RIGHT_4TH_FINGER
        FingerIdentifier.RIGHT_3RD_FINGER -> FingerIdentifierCore.RIGHT_3RD_FINGER
        FingerIdentifier.RIGHT_INDEX_FINGER -> FingerIdentifierCore.RIGHT_INDEX_FINGER
        FingerIdentifier.RIGHT_THUMB -> FingerIdentifierCore.RIGHT_THUMB
        FingerIdentifier.LEFT_THUMB -> FingerIdentifierCore.LEFT_THUMB
        FingerIdentifier.LEFT_INDEX_FINGER -> FingerIdentifierCore.LEFT_INDEX_FINGER
        FingerIdentifier.LEFT_3RD_FINGER -> FingerIdentifierCore.LEFT_3RD_FINGER
        FingerIdentifier.LEFT_4TH_FINGER -> FingerIdentifierCore.LEFT_4TH_FINGER
        FingerIdentifier.LEFT_5TH_FINGER -> FingerIdentifierCore.LEFT_5TH_FINGER
    }
