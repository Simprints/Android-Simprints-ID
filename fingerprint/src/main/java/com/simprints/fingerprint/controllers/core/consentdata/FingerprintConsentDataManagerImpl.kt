package com.simprints.fingerprint.controllers.core.consentdata

import com.simprints.fingerprint.activities.launch.ConsentDataManager

class FingerprintConsentDataManagerImpl(private val consentDataManager: ConsentDataManager): FingerprintConsentDataManager {

    override val parentalConsentExists: Boolean
        get() = consentDataManager.parentalConsentExists

    override val generalConsentOptionsJson: String
        get() = consentDataManager.generalConsentOptionsJson

    override val parentalConsentOptionsJson: String
        get() = consentDataManager.parentalConsentOptionsJson
}
