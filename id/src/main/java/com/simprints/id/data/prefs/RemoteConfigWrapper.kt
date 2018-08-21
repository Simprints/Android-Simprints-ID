package com.simprints.id.data.prefs

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.preferenceType.PrimitivePreference
import org.json.JSONException
import org.json.JSONObject

class RemoteConfigWrapper(private val remoteConfig: FirebaseRemoteConfig, prefs: ImprovedSharedPreferences) {

    var projectSettingsJsonString by PrimitivePreference(prefs, PROJECT_SETTINGS_JSON_STRING_KEY, PROJECT_SETTINGS_JSON_STRING_DEFAULT)

    private val remoteConfigDefaults = mutableMapOf<String, Any>()

    fun <T : Any> prepareDefaultValue(key: String, default: T) {
        remoteConfigDefaults[key] = default
    }

    fun registerAllPreparedDefaultValues() {
        remoteConfig.setDefaults(remoteConfigDefaults)
    }

    fun getString(key: String): String? = getProjectValOtherwiseLocalVal(key, { getString(it) }, { getString(it) })
    fun getBoolean(key: String): Boolean? = getProjectValOtherwiseLocalVal(key, { getBoolean(it) }, { getBoolean(it) })
    fun getLong(key: String): Long? = getProjectValOtherwiseLocalVal(key, { getLong(it) }, { getLong(it) })
    fun getDouble(key: String): Double? = getProjectValOtherwiseLocalVal(key, { getDouble(it) }, { getDouble(it) })

    private inline fun <reified T> getProjectValOtherwiseLocalVal(key: String, remoteConfigGet: FirebaseRemoteConfig.(String) -> T?, jsonGet: JSONObject.(String) -> T?) =
        if (isProjectSpecificMode()) {
            remoteConfig.remoteConfigGet(key)
        } else {
            getValueFromStoredJsonOrNull(key, jsonGet)
        }

    private inline fun <reified T> getValueFromStoredJsonOrNull(key: String, jsonGet: JSONObject.(String) -> T?): T? =
        try {
            JSONObject(projectSettingsJsonString).jsonGet(key)
        } catch (e: JSONException) {
            null
        }

    private fun isProjectSpecificMode(): Boolean {
        return true // TODO
    }

    companion object {
        private const val PROJECT_SETTINGS_JSON_STRING_KEY = "ProjectSettingsJsonString"
        private const val PROJECT_SETTINGS_JSON_STRING_DEFAULT = ""
    }
}
