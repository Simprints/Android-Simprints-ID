package com.simprints.id.data.secure

import androidx.security.crypto.EncryptedSharedPreferences

interface EncryptedSharedPreferencesFactory {

    val encryptedSharedPreferences: EncryptedSharedPreferences
}
