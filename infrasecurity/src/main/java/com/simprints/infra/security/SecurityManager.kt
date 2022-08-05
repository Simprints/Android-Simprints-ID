package com.simprints.infra.security

import android.content.SharedPreferences
import com.simprints.infra.security.exceptions.RootedDeviceException
import com.simprints.infra.security.keyprovider.EncryptedSharedPreferencesBuilder.Companion.FILENAME
import com.simprints.infra.security.keyprovider.LocalDbKey

interface SecurityManager {

    fun buildEncryptedSharedPreferences(filename: String = FILENAME): SharedPreferences

    fun createLocalDatabaseKeyIfMissing(dbName: String)

    fun getLocalDbKeyOrThrow(dbName: String): LocalDbKey

    /**
     * Check if the device is rooted
     * @throws RootedDeviceException
     */
    fun checkIfDeviceIsRooted()

}
