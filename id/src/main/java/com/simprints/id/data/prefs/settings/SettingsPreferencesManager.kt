package com.simprints.id.data.prefs.settings

import com.google.gson.JsonSyntaxException
import com.simprints.id.domain.Constants
import com.simprints.id.services.scheduledSync.peopleDownSync.models.PeopleDownSyncTrigger
import com.simprints.id.tools.serializers.Serializer
import com.simprints.libsimprints.FingerIdentifier


interface SettingsPreferencesManager {

    var nudgeMode: Boolean
    var consent: Boolean
    var qualityThreshold: Int
    var returnIdCount: Int
    var language: String
    var languagePosition: Int
    var projectLanguages: Array<String>
    var matcherType: Int
    var timeoutS: Int
    var moduleIdOptions: Set<String>
    var selectedModules: Set<String>
    var syncGroup: Constants.GROUP
    var matchGroup: Constants.GROUP
    var vibrateMode: Boolean
    var matchingEndWaitTimeSeconds: Int
    /** @throws JsonSyntaxException */
    var fingerStatus: Map<FingerIdentifier, Boolean>

    var syncOnCallout: Boolean

    var scheduledBackgroundSync: Boolean
    var scheduledBackgroundSyncOnlyOnWifi: Boolean
    var scheduledBackgroundSyncOnlyWhenCharging: Boolean
    var scheduledBackgroundSyncOnlyWhenNotLowBattery: Boolean

    var programName: String
    var organizationName: String

    var parentalConsentExists: Boolean
    var generalConsentOptionsJson: String
    var parentalConsentOptionsJson: String

    var peopleDownSyncTriggers: Map<PeopleDownSyncTrigger, Boolean>

    fun getRemoteConfigStringPreference(key: String): String
    fun <T: Any>getRemoteConfigComplexPreference(key: String, serializer: Serializer<T>): T

    fun getRemoteConfigFingerStatus(): Map<FingerIdentifier, Boolean>
}
