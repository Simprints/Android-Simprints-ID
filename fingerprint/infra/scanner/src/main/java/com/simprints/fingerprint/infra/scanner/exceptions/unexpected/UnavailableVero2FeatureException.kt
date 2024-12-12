package com.simprints.fingerprint.infra.scanner.exceptions.unexpected

import com.simprints.fingerprint.infra.scanner.exceptions.ScannerException

class UnavailableVero2FeatureException(
    val feature: UnavailableVero2Feature,
) : ScannerException("UnavailableVero2FeatureException for feature ${feature.name}")

enum class UnavailableVero2Feature {
    IMAGE_ACQUISITION,
    LIVE_FEEDBACK,
}
