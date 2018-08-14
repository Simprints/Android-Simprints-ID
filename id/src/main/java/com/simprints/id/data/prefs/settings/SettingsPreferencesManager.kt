package com.simprints.id.data.prefs.settings

import com.simprints.id.domain.Constants
import com.simprints.id.tools.serializers.Serializer
import com.simprints.libsimprints.FingerIdentifier


interface SettingsPreferencesManager {

    var nudgeMode: Boolean
    var consent: Boolean
    var qualityThreshold: Int
    var returnIdCount: Int
    var language: String
    var languagePosition: Int
    var matcherType: Int
    var timeoutS: Int
    var syncGroup: Constants.GROUP
    var matchGroup: Constants.GROUP
    var vibrateMode: Boolean
    var matchingEndWaitTimeSeconds: Int
    var fingerStatus: Map<FingerIdentifier, Boolean>

    var syncOnCallout: Boolean

    var scheduledBackgroundSync: Boolean
    var scheduledBackgroundSyncOnlyOnWifi: Boolean
    var scheduledBackgroundSyncOnlyWhenCharging: Boolean
    var scheduledBackgroundSyncOnlyWhenNotLowBattery: Boolean

    var programName: String
    var organizationName: String

    var parentalConsent: Boolean
    var generalConsentOptionsJson: String
    var parentalConsentOptionsJson: String

    fun getRemoteConfigStringPreference(key: String): String
    fun <T: Any>getRemoteConfigComplexPreference(key: String, serializer: Serializer<T>): T

    fun getRemoteConfigFingerStatus(): Map<FingerIdentifier, Boolean>
}
