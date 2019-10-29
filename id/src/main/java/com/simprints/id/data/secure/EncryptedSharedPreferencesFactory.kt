package com.simprints.id.data.secure

import android.content.SharedPreferences

interface EncryptedSharedPreferencesFactory {

    val encryptedSharedPreferences: SharedPreferences
}
