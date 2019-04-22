package com.simprints.fingerprint.controllers.consentdata

interface ConsentDataManager {
    var parentalConsentExists: Boolean
    var generalConsentOptionsJson: String
    var parentalConsentOptionsJson: String
}
