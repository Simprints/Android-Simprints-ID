package com.simprints.id.data.secure

import androidx.test.InstrumentationRegistry
import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import com.simprints.id.Application
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.keystore.KeystoreManagerImpl
import com.simprints.id.di.AppModuleForAndroidTests
import com.simprints.id.di.DaggerForAndroidTests
import com.simprints.id.exceptions.safe.secure.MissingLocalDatabaseKeyException
import com.simprints.testframework.common.syntax.anyNotNull
import com.simprints.testframework.common.syntax.assertThrows
import com.simprints.id.tools.RandomGeneratorImpl
import com.simprints.id.tools.delegates.lazyVar
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@SmallTest
class SecureDataManagerTest : DaggerForAndroidTests() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override var module by lazyVar {
        AppModuleForAndroidTests(app)
    }

    @Before
    override fun setUp() {
        app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        super.setUp()
        testAppComponent.inject(this)
        app.initDependencies()
    }

    @Test
    fun createLocalDbKeyForDifferentProjects_shouldProduceDifferentKeys() {

        val keystoreManager = KeystoreManagerImpl(InstrumentationRegistry.getTargetContext())
        val secureDataManager = SecureDataManagerImpl(keystoreManager, preferencesManager, RandomGeneratorImpl())

        secureDataManager.setLocalDatabaseKey("project_id1", "legacy_key")
        val firstLocalDbKey = secureDataManager.getLocalDbKeyOrThrow("project_id1")

        secureDataManager.setLocalDatabaseKey("project_id2", "legacy_key")
        val secondLocalDbKey = secureDataManager.getLocalDbKeyOrThrow("project_id2")

        assertNotSame(firstLocalDbKey, secondLocalDbKey)
    }

    @Test
    fun createLocalDbKeysForSameProjectId_shouldProduceTheSameLocalKey() {

        val keystoreManager = KeystoreManagerImpl(InstrumentationRegistry.getTargetContext())
        val secureDataManager = SecureDataManagerImpl(keystoreManager, preferencesManager)

        secureDataManager.setLocalDatabaseKey("project_id3", "legacy_key")
        val firstLocalDbKey = secureDataManager.getLocalDbKeyOrThrow("project_id3")

        secureDataManager.setLocalDatabaseKey("project_id3", "legacy_key")
        val secondLocalDbKey = secureDataManager.getLocalDbKeyOrThrow("project_id3")

        assertEquals(firstLocalDbKey, secondLocalDbKey)
    }

    @Test
    fun noLocalKey_shouldThrowAnError() {

        val keystoreManager = KeystoreManagerImpl(InstrumentationRegistry.getTargetContext())
        val secureDataManager = SecureDataManagerImpl(keystoreManager, preferencesManager)

        assertThrows<MissingLocalDatabaseKeyException> {
            secureDataManager.getLocalDbKeyOrThrow("project_id4")
        }
    }

    @Test
    fun invalidEncryptedData_shouldThrowAnError() {

        val keystoreManager = spy(KeystoreManagerImpl(InstrumentationRegistry.getTargetContext()))
        doReturn("wrong_encryption").`when`(keystoreManager).encryptString(anyNotNull())
        val secureDataManager = SecureDataManagerImpl(keystoreManager, preferencesManager)

        assertThrows<MissingLocalDatabaseKeyException> {
            secureDataManager.getLocalDbKeyOrThrow("project_id4")
        }
    }
}
