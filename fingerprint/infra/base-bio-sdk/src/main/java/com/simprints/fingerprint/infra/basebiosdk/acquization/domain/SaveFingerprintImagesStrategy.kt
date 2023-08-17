package com.simprints.fingerprint.infra.basebiosdk.acquization.domain

enum class SaveFingerprintImagesStrategy {
    NEVER,          // Never save fingerprint images
    WSQ_15,         // Save enrolled images using WSQ with 15x compression
    WSQ_15_EAGER    // Save ALL captured images using WSQ with 15x compression
}
