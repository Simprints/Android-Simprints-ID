package com.simprints.id.data.secure

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys



class EncryptedSharedPreferencesBuilderImpl(val ctx: Context): EncryptedSharedPreferencesBuilder {


    var masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    override fun buildEncryptedSharedPreferences(filename: String) =
        EncryptedSharedPreferences
            .create(
                filename,
                masterKeyAlias,
                ctx,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ) as EncryptedSharedPreferences
}
