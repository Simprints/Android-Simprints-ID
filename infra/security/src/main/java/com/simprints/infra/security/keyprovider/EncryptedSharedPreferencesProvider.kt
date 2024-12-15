package com.simprints.infra.security.keyprovider

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences

internal class EncryptedSharedPreferencesProvider(
    private val ctx: Context,
) {
    fun provideEncryptedSharedPreferences(
        filename: String,
        masterKeyAlias: String,
    ): SharedPreferences = EncryptedSharedPreferences
        .create(
            filename,
            masterKeyAlias,
            ctx,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
}
