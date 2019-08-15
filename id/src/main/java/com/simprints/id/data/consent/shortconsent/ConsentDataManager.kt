package com.simprints.id.data.consent.shortconsent

interface ConsentDataManager {
    var parentalConsentExists: Boolean
    var generalConsentOptionsJson: String
    var parentalConsentOptionsJson: String
}
