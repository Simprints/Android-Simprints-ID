package com.simprints.id.data.secure

import android.content.SharedPreferences

interface EncryptedSharedPreferencesBuilder {
    companion object {
        const val FILENAME = "encrypted_shared"
    }

    fun buildEncryptedSharedPreferences(filename: String = FILENAME): SharedPreferences
}
