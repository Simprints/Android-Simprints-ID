package com.simprints.id.data.prefs.settings

import com.google.gson.JsonSyntaxException
import com.simprints.id.FingerIdentifier
import com.simprints.id.domain.GROUP
import com.simprints.id.services.scheduledSync.peopleDownSync.models.PeopleDownSyncTrigger
import com.simprints.id.tools.serializers.Serializer


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
    var syncGroup: GROUP
    var matchGroup: GROUP
    var vibrateMode: Boolean
    var matchingEndWaitTimeSeconds: Int
    /** @throws JsonSyntaxException */
    var fingerStatus: Map<FingerIdentifier, Boolean>

    var programName: String
    var organizationName: String

    var logoExists: Boolean
    var parentalConsentExists: Boolean
    var generalConsentOptionsJson: String
    var parentalConsentOptionsJson: String

    var peopleDownSyncTriggers: Map<PeopleDownSyncTrigger, Boolean>

    fun getRemoteConfigStringPreference(key: String): String
    fun <T: Any>getRemoteConfigComplexPreference(key: String, serializer: Serializer<T>): T

    fun getRemoteConfigFingerStatus(): Map<FingerIdentifier, Boolean>
}
