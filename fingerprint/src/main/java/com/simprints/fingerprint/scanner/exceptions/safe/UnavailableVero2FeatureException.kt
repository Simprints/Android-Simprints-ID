package com.simprints.fingerprint.scanner.exceptions.safe

import com.simprints.fingerprint.exceptions.FingerprintSimprintsException

class UnavailableVero2FeatureException(val feature: UnavailableVero2Feature)
    : FingerprintSimprintsException("UnavailableVero2FeatureException for feature ${feature.name}")

enum class UnavailableVero2Feature {
    IMAGE_ACQUISITION
}
