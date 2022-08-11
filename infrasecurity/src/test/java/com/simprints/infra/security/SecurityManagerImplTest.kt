package com.simprints.infra.security

import com.simprints.infra.security.keyprovider.EncryptedSharedPreferencesBuilder
import com.simprints.infra.security.keyprovider.SecureLocalDbKeyProvider
import com.simprints.infra.security.root.RootManager
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test

class SecurityManagerImplTest {

    @Test
    fun `calling build encrypted prefs should call the pref builder`() {
        val encryptedSharedPreferencesBuilder: EncryptedSharedPreferencesBuilder = spyk()
        val securityManager = SecurityManagerImpl(encryptedSharedPreferencesBuilder, mockk(), mockk())

        val fileName = "testFileName"

        securityManager.buildEncryptedSharedPreferences(fileName)

        verify(exactly = 1) { encryptedSharedPreferencesBuilder.buildEncryptedSharedPreferences(fileName) }
    }

    @Test
    fun `calling create db prefs should call the secure db provider`() {
        val secureLocalDbKeyProvider: SecureLocalDbKeyProvider = spyk()
        val securityManager = SecurityManagerImpl(mockk(),secureLocalDbKeyProvider, mockk())

        val fileName = "testFileName"

        securityManager.createLocalDatabaseKeyIfMissing(fileName)

        verify(exactly = 1) { secureLocalDbKeyProvider.createLocalDatabaseKeyIfMissing(fileName) }
    }

    @Test
    fun `calling get db key should call the secure db provider`() {
        val secureLocalDbKeyProvider: SecureLocalDbKeyProvider = spyk()
        val securityManager = SecurityManagerImpl(mockk(),secureLocalDbKeyProvider, mockk())

        val fileName = "testFileName"

        securityManager.getLocalDbKeyOrThrow(fileName)

        verify(exactly = 1) { secureLocalDbKeyProvider.getLocalDbKeyOrThrow(fileName) }
    }

    @Test
    fun `calling check device root prefs should call the root manager`() {
        val rootManager: RootManager = spyk()
        val securityManager = SecurityManagerImpl(mockk(), mockk(), rootManager)

        securityManager.checkIfDeviceIsRooted()

        verify(exactly = 1) { rootManager.checkIfDeviceIsRooted() }
    }
    
}
