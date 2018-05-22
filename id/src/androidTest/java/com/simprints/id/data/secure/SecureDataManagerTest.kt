package com.simprints.id.data.secure

import android.support.test.InstrumentationRegistry
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import com.simprints.id.Application
import com.simprints.id.data.secure.keystore.KeystoreManagerImpl
import com.simprints.id.exceptions.unsafe.MissingLocalDatabaseKey
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.assertThrows
import com.simprints.id.tools.RandomGeneratorImpl
import junit.framework.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy

@RunWith(AndroidJUnit4::class)
@SmallTest
class SecureDataManagerTest {

    @Test
    fun createLocalDbKeyForDifferentProjects_shouldProduceDifferentKeys() {

        val app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        val keystoreManager = KeystoreManagerImpl(InstrumentationRegistry.getTargetContext())
        val secureDataManager = SecureDataManagerImpl(keystoreManager, app.preferencesManager, RandomGeneratorImpl())

        secureDataManager.setLocalDatabaseKey("project_id1", "legacy_key")
        val firstLocalDbKey = secureDataManager.getLocalDbKeyOrThrow("project_id1")

        secureDataManager.setLocalDatabaseKey("project_id2", "legacy_key")
        val secondLocalDbKey = secureDataManager.getLocalDbKeyOrThrow("project_id2")

        Assert.assertNotSame(firstLocalDbKey, secondLocalDbKey)
    }

    @Test
    fun createLocalDbKeysForSameProjectId_shouldProduceTheSameLocalKey() {

        val app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        val keystoreManager = KeystoreManagerImpl(InstrumentationRegistry.getTargetContext())
        val secureDataManager = SecureDataManagerImpl(keystoreManager, app.preferencesManager)

        secureDataManager.setLocalDatabaseKey("project_id3", "legacy_key")
        val firstLocalDbKey = secureDataManager.getLocalDbKeyOrThrow("project_id3")

        secureDataManager.setLocalDatabaseKey("project_id3", "legacy_key")
        val secondLocalDbKey = secureDataManager.getLocalDbKeyOrThrow("project_id3")

        Assert.assertEquals(firstLocalDbKey, secondLocalDbKey)
    }

    @Test
    fun noLocalKey_shouldThrowAnError() {

        val app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        val keystoreManager = KeystoreManagerImpl(InstrumentationRegistry.getTargetContext())
        val secureDataManager = SecureDataManagerImpl(keystoreManager, app.preferencesManager)

        assertThrows<MissingLocalDatabaseKey> {
            secureDataManager.getLocalDbKeyOrThrow("project_id4")
        }
    }

    @Test
    fun invalidEncryptedData_shouldThrowAnError() {

        val app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        val keystoreManager = spy(KeystoreManagerImpl(InstrumentationRegistry.getTargetContext()))
        doReturn("wrong_encryption").`when`(keystoreManager).encryptString(anyNotNull())
        val secureDataManager = SecureDataManagerImpl(keystoreManager, app.preferencesManager)

        assertThrows<MissingLocalDatabaseKey> {
            secureDataManager.getLocalDbKeyOrThrow("project_id4")
        }
    }
}
