package com.simprints.infra.security

import android.content.SharedPreferences
import com.simprints.infra.security.keyprovider.EncryptedSharedPreferencesBuilder
import com.simprints.infra.security.keyprovider.LocalDbKey
import com.simprints.infra.security.keyprovider.SecureLocalDbKeyProvider
import com.simprints.infra.security.root.RootManager
import javax.inject.Inject

internal class SecurityManagerImpl @Inject constructor(
    private val encryptedSharedPreferencesBuilder: EncryptedSharedPreferencesBuilder,
    private val secureLocalDbKeyProvider: SecureLocalDbKeyProvider,
    private val rootManager: RootManager
) : SecurityManager {

    override fun buildEncryptedSharedPreferences(filename: String): SharedPreferences =
        encryptedSharedPreferencesBuilder.buildEncryptedSharedPreferences(filename)

    override fun createLocalDatabaseKeyIfMissing(dbName: String) {
        secureLocalDbKeyProvider.createLocalDatabaseKeyIfMissing(dbName)
    }

    override fun getLocalDbKeyOrThrow(dbName: String): LocalDbKey =
        secureLocalDbKeyProvider.getLocalDbKeyOrThrow(dbName)

    override fun checkIfDeviceIsRooted() {
        rootManager.checkIfDeviceIsRooted()
    }

}
