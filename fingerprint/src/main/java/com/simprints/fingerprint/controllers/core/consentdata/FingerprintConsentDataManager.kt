package com.simprints.fingerprint.controllers.core.consentdata

interface FingerprintConsentDataManager {
    val parentalConsentExists: Boolean
    val generalConsentOptionsJson: String
    val parentalConsentOptionsJson: String
}
