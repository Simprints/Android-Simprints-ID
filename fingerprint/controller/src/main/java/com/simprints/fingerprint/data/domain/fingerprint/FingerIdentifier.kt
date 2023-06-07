package com.simprints.fingerprint.data.domain.fingerprint

import com.simprints.moduleapi.fingerprint.IFingerIdentifier

/**
 * enum class to represent a subject's fingers
 */
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
    LEFT_5TH_FINGER;
}

/**
 * an extension function to help convert moduleapi's IFingerprintIdentifier to fingerprint's
 * FingerprintIdentifier
 */
fun IFingerIdentifier.fromModuleApiToDomain(): FingerIdentifier =
    when (this) {
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


/**
 * an extension function to help convert fingerprint's FingerprintIdentifier to moduleapi's
 * IFingerprintIdentifier
 */
fun FingerIdentifier.fromDomainToModuleApi(): IFingerIdentifier =
    when (this) {
        FingerIdentifier.RIGHT_5TH_FINGER -> IFingerIdentifier.RIGHT_5TH_FINGER
        FingerIdentifier.RIGHT_4TH_FINGER -> IFingerIdentifier.RIGHT_4TH_FINGER
        FingerIdentifier.RIGHT_3RD_FINGER -> IFingerIdentifier.RIGHT_3RD_FINGER
        FingerIdentifier.RIGHT_INDEX_FINGER -> IFingerIdentifier.RIGHT_INDEX_FINGER
        FingerIdentifier.RIGHT_THUMB -> IFingerIdentifier.RIGHT_THUMB
        FingerIdentifier.LEFT_THUMB -> IFingerIdentifier.LEFT_THUMB
        FingerIdentifier.LEFT_INDEX_FINGER -> IFingerIdentifier.LEFT_INDEX_FINGER
        FingerIdentifier.LEFT_3RD_FINGER -> IFingerIdentifier.LEFT_3RD_FINGER
        FingerIdentifier.LEFT_4TH_FINGER -> IFingerIdentifier.LEFT_4TH_FINGER
        FingerIdentifier.LEFT_5TH_FINGER -> IFingerIdentifier.LEFT_5TH_FINGER
    }
