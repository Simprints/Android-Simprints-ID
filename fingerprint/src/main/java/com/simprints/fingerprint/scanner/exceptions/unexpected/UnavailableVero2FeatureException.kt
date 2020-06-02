package com.simprints.fingerprint.scanner.exceptions.unexpected

import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException

class UnavailableVero2FeatureException(val feature: UnavailableVero2Feature)
    : FingerprintUnexpectedException("UnavailableVero2FeatureException for feature ${feature.name}")

enum class UnavailableVero2Feature {
    IMAGE_ACQUISITION,
    OTA
}
