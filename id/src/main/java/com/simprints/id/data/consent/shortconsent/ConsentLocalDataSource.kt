package com.simprints.id.data.consent.shortconsent

interface ConsentLocalDataSource {
    var parentalConsentExists: Boolean
    var generalConsentOptionsJson: String
    var parentalConsentOptionsJson: String
}
