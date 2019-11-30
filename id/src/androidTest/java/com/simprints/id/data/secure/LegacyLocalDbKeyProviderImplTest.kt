package com.simprints.id.data.secure

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.Application
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.keystore.KeystoreManagerImpl
import com.simprints.id.exceptions.safe.secure.MissingLocalDatabaseKeyException
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class LegacyLocalDbKeyProviderImplTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()

    @Inject lateinit var preferencesManager: PreferencesManager

    @Before
    fun setUp() {
        AndroidTestConfig(this).fullSetup()
    }

    @Test
    fun noLocalKey_shouldThrowAnError() {

        val keystoreManager = KeystoreManagerImpl(app)
        val secureDataManager = LegacyLocalDbKeyProviderImpl(keystoreManager, preferencesManager)

        assertThrows<MissingLocalDatabaseKeyException> {
            secureDataManager.getLocalDbKeyOrThrow("project_id4")
        }
    }

    @Test
    fun invalidEncryptedData_shouldThrowAnError() {

        val keystoreManager = spy(KeystoreManagerImpl(app))
        doReturn("wrong_encryption").`when`(keystoreManager).encryptString(anyNotNull())
        val secureDataManager = LegacyLocalDbKeyProviderImpl(keystoreManager, preferencesManager)

        assertThrows<MissingLocalDatabaseKeyException> {
            secureDataManager.getLocalDbKeyOrThrow("project_id4")
        }
    }
}
