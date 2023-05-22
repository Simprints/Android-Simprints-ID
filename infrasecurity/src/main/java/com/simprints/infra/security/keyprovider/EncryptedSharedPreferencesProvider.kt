package com.simprints.infra.security.keyprovider

import android.content.SharedPreferences

interface EncryptedSharedPreferencesProvider {
    fun provideEncryptedSharedPreferences(filename: String, masterKeyAlias: String): SharedPreferences
}
