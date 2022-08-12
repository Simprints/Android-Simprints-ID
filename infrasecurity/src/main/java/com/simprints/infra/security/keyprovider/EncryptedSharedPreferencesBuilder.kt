package com.simprints.infra.security.keyprovider

import android.content.SharedPreferences

internal interface EncryptedSharedPreferencesBuilder {
    companion object {
        const val SHARED_PREFS_FILENAME = "encrypted_shared"
    }

    fun buildEncryptedSharedPreferences(filename: String): SharedPreferences
}
