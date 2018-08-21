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

    fun getString(key: String): String? = remoteConfig.getString(key)
    fun getBoolean(key: String): Boolean? = remoteConfig.getBoolean(key)
    fun getLong(key: String): Long? = remoteConfig.getLong(key)
    fun getDouble(key: String): Double? = remoteConfig.getDouble(key)
    fun getByteArray(key: String): ByteArray? = remoteConfig.getByteArray(key)

}
