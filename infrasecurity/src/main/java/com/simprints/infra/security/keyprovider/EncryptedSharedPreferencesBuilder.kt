package com.simprints.infra.security.keyprovider

import android.content.SharedPreferences

internal interface EncryptedSharedPreferencesBuilder {

    fun buildEncryptedSharedPreferences(filename: String): SharedPreferences

}
