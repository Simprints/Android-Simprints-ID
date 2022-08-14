package com.simprints.infra.security

import android.content.SharedPreferences
import com.simprints.infra.security.exceptions.RootedDeviceException
import com.simprints.infra.security.keyprovider.EncryptedSharedPreferencesBuilder.Companion.SHARED_PREFS_FILENAME
import com.simprints.infra.security.keyprovider.LocalDbKey

interface SecurityManager {

    /**
     * If no argument is supplied for filename then it uses the shared encrypted shared prefs file
     */
    fun buildEncryptedSharedPreferences(filename: String = SHARED_PREFS_FILENAME): SharedPreferences

    fun createLocalDatabaseKeyIfMissing(dbName: String)

    fun getLocalDbKeyOrThrow(dbName: String): LocalDbKey

    /**
     * Check if the device is rooted
     * @throws RootedDeviceException
     */
    fun checkIfDeviceIsRooted()

}
