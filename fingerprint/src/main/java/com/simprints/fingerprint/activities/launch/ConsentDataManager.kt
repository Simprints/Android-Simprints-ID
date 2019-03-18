package com.simprints.fingerprint.activities.launch

interface ConsentDataManager {
    var parentalConsentExists: Boolean
    var generalConsentOptionsJson: String
    var parentalConsentOptionsJson: String
}
