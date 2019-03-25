package com.simprints.fingerprint.activities.launch

import com.simprints.core.tools.json.JsonHelper
import com.simprints.fingerprint.data.domain.consent.GeneralConsent
import com.simprints.fingerprint.data.domain.consent.ParentalConsent
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.preferenceType.remoteConfig.RemoteConfigPrimitivePreference

open class ConsentDataManagerImpl(
    prefs: ImprovedSharedPreferences,
    remoteConfigWrapper: RemoteConfigWrapper)
    : ConsentDataManager {

    companion object {

        const val PARENTAL_CONSENT_EXISTS_KEY = "ConsentParentalExists"
        const val PARENTAL_CONSENT_EXISTS_DEFAULT = false

        const val GENERAL_CONSENT_OPTIONS_JSON_KEY = "ConsentGeneralOptions"
        val GENERAL_CONSENT_OPTIONS_JSON_DEFAULT: String = JsonHelper.toJson(GeneralConsent())

        const val PARENTAL_CONSENT_OPTIONS_JSON_KEY = "ConsentParentalOptions"
        val PARENTAL_CONSENT_OPTIONS_JSON_DEFAULT: String = JsonHelper.toJson(ParentalConsent())
    }

    // Whether the parental consent should be shown
    override var parentalConsentExists: Boolean
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, PARENTAL_CONSENT_EXISTS_KEY, PARENTAL_CONSENT_EXISTS_DEFAULT)
    // The options of the general consent as a JSON string of booleans
    override var generalConsentOptionsJson: String
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, GENERAL_CONSENT_OPTIONS_JSON_KEY, GENERAL_CONSENT_OPTIONS_JSON_DEFAULT)
    // The options of the parental consent as a JSON string of booleans
    override var parentalConsentOptionsJson: String
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, PARENTAL_CONSENT_OPTIONS_JSON_KEY, PARENTAL_CONSENT_OPTIONS_JSON_DEFAULT)
}
