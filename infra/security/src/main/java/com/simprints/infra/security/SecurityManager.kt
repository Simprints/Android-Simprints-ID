package com.simprints.infra.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedFile
import com.simprints.infra.security.exceptions.RootedDeviceException
import com.simprints.infra.security.keyprovider.LocalDbKey
import java.io.File

interface SecurityManager {
    companion object {
        /**
         * This is a global shared prefs file other modules can use to store values. Keep in mind
         * that if two modules use the same field name then they will overwrite each others values,
         * so it is advised to use module specific shared preference files.
         */
        const val GLOBAL_SHARED_PREFS_FILENAME = "encrypted_shared"
    }

    fun buildEncryptedSharedPreferences(filename: String): SharedPreferences

    fun createLocalDatabaseKeyIfMissing(dbName: String)

    fun recreateLocalDatabaseKey(dbName: String)

    fun getLocalDbKeyOrThrow(dbName: String): LocalDbKey

    /**
     * Check if the device is rooted
     * @throws RootedDeviceException
     */
    fun checkIfDeviceIsRooted()

    fun getEncryptedFileBuilder(
        file: File,
        context: Context,
    ): EncryptedFile
}
