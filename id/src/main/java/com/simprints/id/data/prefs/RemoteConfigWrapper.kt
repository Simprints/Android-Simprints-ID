package com.simprints.id.data.prefs

import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class RemoteConfigWrapper(private val remoteConfig: FirebaseRemoteConfig) {

    private val remoteConfigDefaults = mutableMapOf<String, Any>()

    fun <T : Any> prepareDefaultValue(key: String, default: T) {
        remoteConfigDefaults[key] = default
    }

    fun registerAllPreparedDefaultValues() {
        remoteConfig.setDefaults(remoteConfigDefaults)
    }

    fun getString(key: String): String? = getProjectValOtherwiseLocalVal(key, { getString(it) }, { getLocalString(it) })
    fun getBoolean(key: String): Boolean? = getProjectValOtherwiseLocalVal(key, { getBoolean(it) }, { getLocalBoolean(it) })
    fun getLong(key: String): Long? = getProjectValOtherwiseLocalVal(key, { getLong(it) }, { getLocalLong(it) })
    fun getDouble(key: String): Double? = getProjectValOtherwiseLocalVal(key, { getDouble(it) }, { getLocalDouble(it) })

    private inline fun <reified T> getProjectValOtherwiseLocalVal(key: String, remoteGet: FirebaseRemoteConfig.(String) -> T?, localGet: (String) -> T?) =
        if (isProjectSpecificMode()) {
            remoteConfig.remoteGet(key)
        } else {
            localGet(key)
        }

    private fun isProjectSpecificMode(): Boolean {
        return true
    }

    private fun getLocalString(key: String): String? {
        TODO()
    }

    private fun getLocalBoolean(key: String): Boolean? {
        TODO()
    }

    private fun getLocalLong(key: String): Long? {
        TODO()
    }

    private fun getLocalDouble(key: String): Double? {
        TODO()
    }
}
