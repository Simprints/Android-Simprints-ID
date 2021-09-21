package com.simprints.id.data.prefs

import com.simprints.core.sharedpreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.preferenceType.PrimitivePreference
import org.json.JSONException
import org.json.JSONObject

class RemoteConfigWrapper(prefs: ImprovedSharedPreferences) {

    var projectSettingsJsonString by PrimitivePreference(
        prefs,
        PROJECT_SETTINGS_JSON_STRING_KEY,
        PROJECT_SETTINGS_JSON_STRING_DEFAULT
    )

    fun clearRemoteConfig() {
        projectSettingsJsonString = ""
    }

    fun getString(key: String): String? = getValueFromStoredJsonOrNull(key) { getString(it) }
    fun getBoolean(key: String): Boolean? = getValueFromStoredJsonOrNull(key) { getBoolean(it) }
    fun getLong(key: String): Long? = getValueFromStoredJsonOrNull(key) { getLong(it) }
    fun getDouble(key: String): Double? = getValueFromStoredJsonOrNull(key) { getDouble(it) }

    private inline fun <reified T> getValueFromStoredJsonOrNull(
        key: String,
        jsonGet: JSONObject.(String) -> T?
    ): T? =
        try {
            JSONObject(projectSettingsJsonString).jsonGet(key)
        } catch (e: JSONException) {
            null
        }

    companion object {
        const val PROJECT_SETTINGS_JSON_STRING_KEY = "ProjectSettingsJsonString"
        const val PROJECT_SETTINGS_JSON_STRING_DEFAULT = ""
    }
}
