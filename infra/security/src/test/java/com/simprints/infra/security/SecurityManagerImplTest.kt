package com.simprints.infra.security

import com.simprints.infra.security.cryptography.MasterKeyHelper
import com.simprints.infra.security.keyprovider.EncryptedSharedPreferencesBuilder
import com.simprints.infra.security.keyprovider.SecureLocalDbKeyProvider
import com.simprints.infra.security.root.RootManager
import io.mockk.*
import org.junit.Test

class SecurityManagerImplTest {
    @Test
    fun `calling build encrypted prefs should call the pref builder`() {
        val encryptedSharedPreferencesBuilder: EncryptedSharedPreferencesBuilder = spyk()
        val securityManager =
            SecurityManagerImpl(encryptedSharedPreferencesBuilder, mockk(), mockk(), mockk())

        val fileName = "testFileName"

        securityManager.buildEncryptedSharedPreferences(fileName)

        verify(exactly = 1) {
            encryptedSharedPreferencesBuilder.buildEncryptedSharedPreferences(
                fileName,
            )
        }
    }

    @Test
    fun `calling create db prefs should call the secure db provider`() {
        val secureLocalDbKeyProvider: SecureLocalDbKeyProvider = spyk()
        val securityManager =
            SecurityManagerImpl(mockk(), secureLocalDbKeyProvider, mockk(), mockk())

        val fileName = "testFileName"

        securityManager.createLocalDatabaseKeyIfMissing(fileName)

        verify(exactly = 1) { secureLocalDbKeyProvider.createLocalDatabaseKeyIfMissing(fileName) }
    }

    @Test
    fun `calling get db key should call the secure db provider`() {
        val secureLocalDbKeyProvider: SecureLocalDbKeyProvider = spyk()
        val securityManager =
            SecurityManagerImpl(mockk(), secureLocalDbKeyProvider, mockk(), mockk())

        val fileName = "testFileName"

        securityManager.getLocalDbKeyOrThrow(fileName)

        verify(exactly = 1) { secureLocalDbKeyProvider.getLocalDbKeyOrThrow(fileName) }
    }

    @Test
    fun `calling check device root prefs should call the root manager`() {
        val rootManager: RootManager = spyk()
        val securityManager = SecurityManagerImpl(mockk(), mockk(), rootManager, mockk())

        securityManager.checkIfDeviceIsRooted()

        verify(exactly = 1) { rootManager.checkIfDeviceIsRooted() }
    }

    @Test
    fun `calling getEncryptedFileBuilder should call the master key helper`() {
        val masterKeyHelperMock: MasterKeyHelper = mockk {
            every { getEncryptedFileBuilder(any(), any()) } returns mockk()
        }
        val securityManager = SecurityManagerImpl(mockk(), mockk(), mockk(), masterKeyHelperMock)

        securityManager.getEncryptedFileBuilder(mockk(), mockk())

        verify(exactly = 1) { masterKeyHelperMock.getEncryptedFileBuilder(any(), any()) }
    }

    @Test
    fun `test recreateLocalDatabaseKey`() {
        // Given
        val dbName = "dbName"
        val secureLocalDbKeyProvider = mockk<SecureLocalDbKeyProvider> {
            justRun { recreateLocalDatabaseKey(dbName) }
        }
        val securityManager =
            SecurityManagerImpl(mockk(), secureLocalDbKeyProvider, mockk(), mockk())
        // When
        securityManager.recreateLocalDatabaseKey(dbName)
        // Then
        verify { secureLocalDbKeyProvider.recreateLocalDatabaseKey(dbName) }
    }
}
