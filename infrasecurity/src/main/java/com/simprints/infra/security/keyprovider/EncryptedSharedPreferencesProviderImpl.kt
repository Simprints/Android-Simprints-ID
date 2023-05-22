package com.simprints.infra.security.keyprovider

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class EncryptedSharedPreferencesProviderImpl @Inject constructor(@ApplicationContext private val ctx: Context) :
    EncryptedSharedPreferencesProvider {
    override fun provideEncryptedSharedPreferences(
        filename: String,
        masterKeyAlias: String
    ): SharedPreferences = EncryptedSharedPreferences
        .create(
            filename,
            masterKeyAlias,
            ctx,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
}
