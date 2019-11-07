package com.simprints.id.data.prefs.settings

import com.google.gson.JsonSyntaxException
import com.simprints.id.FingerIdentifier
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modality
import com.simprints.id.services.scheduledSync.peopleDownSync.models.PeopleDownSyncTrigger
import com.simprints.id.tools.serializers.Serializer


interface SettingsPreferencesManager {

    var returnIdCount: Int
    var language: String
    var projectLanguages: Array<String>
    var moduleIdOptions: Set<String>
    var selectedModules: Set<String>
    var maxNumberOfModules: Int
    var syncGroup: GROUP
    var matchGroup: GROUP
    /** @throws JsonSyntaxException */
    var fingerStatus: Map<FingerIdentifier, Boolean>

    var programName: String
    var organizationName: String

    var logoExists: Boolean

    var modality: Modality
    var peopleDownSyncTriggers: Map<PeopleDownSyncTrigger, Boolean>

    fun getRemoteConfigStringPreference(key: String): String
    fun <T: Any>getRemoteConfigComplexPreference(key: String, serializer: Serializer<T>): T

    fun getRemoteConfigFingerStatus(): Map<FingerIdentifier, Boolean>
}
