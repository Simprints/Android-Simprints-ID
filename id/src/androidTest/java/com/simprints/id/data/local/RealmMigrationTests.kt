package com.simprints.id.data.local

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.simprints.id.data.db.local.LocalDbKey
import com.simprints.id.data.db.local.RealmConfig
import com.simprints.id.data.db.local.RealmDbManager
import com.simprints.id.data.db.local.models.rl_Fingerprint
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.id.tools.extensions.awaitAndAssertSuccess
import com.simprints.id.tools.extensions.toHexString
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmList
import junit.framework.Assert
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber
import java.io.File
import java.util.*

@RunWith(AndroidJUnit4::class)
class RealmMigrationTests {

    companion object {
        private const val KEY_LENGTH = 64

        const val legacyDatabaseName: String = "legacyDB"
        const val newDatabaseName: String = "newDatabase"
        val newDatabaseKey: ByteArray = Arrays.copyOf("newKey".toByteArray(), KEY_LENGTH)

        const val FAKE_STRING_FIELD: String = "123"
    }

    private val localDbKey = LocalDbKey(newDatabaseName, newDatabaseKey, legacyDatabaseName)
    private val legacyConfig = RealmConfig.get(localDbKey.legacyApiKey, localDbKey.legacyRealmKey)
    private val newConfig = RealmConfig.get(localDbKey.projectId, localDbKey.value)

    private val testContext = InstrumentationRegistry.getTargetContext()

    init {
        Timber.d("New Key: ${localDbKey.value.toHexString()}")
        Timber.d("Old Key: ${localDbKey.legacyRealmKey.toHexString()}")
    }

    @Test
    fun localSignInWithEncryptionChange_ShouldMigrateDatabase() {
        Realm.getInstance(legacyConfig).use { addFakePatient(it) }

        RealmDbManager(testContext).signInToLocal(localDbKey).test()
            .awaitAndAssertSuccess()
            .let {
                Assert.assertTrue(!File(legacyConfig.path).exists())
                Assert.assertTrue(File(newConfig.path).exists())
            }
    }

    @Test
    fun localSignInWithoutEncryptionChange_ShouldNotEffectNewDatabase() {
        RealmDbManager(testContext).signInToLocal(localDbKey).test()
            .awaitAndAssertSuccess()
            .let {
                assert(!File(legacyConfig.path).exists())
                assert(File(newConfig.path).exists())
            }
    }

    @Test
    fun localSignInWithEncryptionChange_ShouldCloseLegacyRealm() {
        Realm.getInstance(newConfig).use { addFakePatient(it) }

        RealmDbManager(testContext).signInToLocal(localDbKey).test()
            .awaitAndAssertSuccess()
            .let {
                assert(Realm.getInstance(newConfig).use { it.isClosed })
            }
    }

    @Test
    fun localSignInWithEncryptionChange_ShouldHaveValidData() {
        Realm.getInstance(legacyConfig).use { addFakePatient(it) }

        RealmDbManager(testContext).signInToLocal(localDbKey).test()
            .awaitAndAssertSuccess()
            .let {
                Realm.getInstance(newConfig).use {
                    val newPerson = it.where(rl_Person::class.java).findFirst()
                    Assert.assertNotNull(newPerson)
                    Assert.assertTrue(newPerson!!.isFakePatientModel())
                }
            }
    }

    @After
    fun cleanUp() {
        deleteRealmFiles(legacyConfig)
        deleteRealmFiles(newConfig)
    }

    private fun getFakePatientModel() = rl_Person().apply {
        patientId = FAKE_STRING_FIELD
        userId = FAKE_STRING_FIELD
        moduleId = FAKE_STRING_FIELD
        projectId = FAKE_STRING_FIELD
        @Suppress("RemoveExplicitTypeArguments")
        fingerprints = RealmList<rl_Fingerprint>()
    }

    private fun rl_Person.isFakePatientModel(): Boolean = when {
        this.projectId != getFakePatientModel().projectId -> false
        this.userId != getFakePatientModel().userId -> false
        this.moduleId != getFakePatientModel().moduleId -> false
        else -> true
    }

    private fun addFakePatient(realm: Realm) {
        realm.executeTransaction {
            it.insertOrUpdate(getFakePatientModel())
        }
    }

    private fun deleteRealmFiles(realmConfig: RealmConfiguration) {
        Realm.deleteRealm(realmConfig)
        File("${realmConfig.path}.lock").delete()
    }

}
