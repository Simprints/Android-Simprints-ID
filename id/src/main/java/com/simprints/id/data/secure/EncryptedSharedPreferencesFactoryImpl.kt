package com.simprints.id.data.secure

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences

class EncryptedSharedPreferencesFactoryImpl(ctx: Context): EncryptedSharedPreferencesFactory {

    companion object {
        private const val FILENAME = "encrypted_shared"
        private const val MASTER_KEY_ALIAS = "master_key_alias_for_shared_preferences"

    }

    override val encryptedSharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences
            .create(
                FILENAME,
                MASTER_KEY_ALIAS,
                ctx,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
    }
}
