package com.simprints.id.data.local

import android.support.test.runner.AndroidJUnit4
import com.simprints.id.data.db.local.realm.RealmConfig
import com.simprints.id.data.db.local.realm.RealmDbManagerImpl
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.tools.extensions.awaitAndAssertSuccess
import io.realm.Realm
import junit.framework.Assert
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File


@RunWith(AndroidJUnit4::class)
class RealmEncryptionChangeTests : RealmTestsBase() {

    private val legacyConfig = RealmConfig.get(localDbKey.legacyApiKey, localDbKey.legacyRealmKey)

    init {
        deleteRealmFiles(legacyConfig)
    }

    @Test
    fun localSignInWithEncryptionChange_ShouldMigrateDatabase() {
        Realm.getInstance(legacyConfig).use { saveFakePerson(it, getFakePerson()) }

        RealmDbManagerImpl(testContext, TestProjectIdProvider(), TestLocalDbKeyProvider())
            .signInToLocal()
            .test()
            .awaitAndAssertSuccess()
            .let {
                Assert.assertTrue(!File(legacyConfig.path).exists())
                Assert.assertTrue(File(config.path).exists())
            }
    }

    @Test
    fun localSignInWithoutEncryptionChange_ShouldNotEffectNewDatabase() {
        RealmDbManagerImpl(testContext, TestProjectIdProvider(), TestLocalDbKeyProvider())
            .signInToLocal()
            .test()
            .awaitAndAssertSuccess()
            .let {
                assert(!File(legacyConfig.path).exists())
                assert(File(config.path).exists())
            }
    }

    @Test
    fun localSignInWithEncryptionChange_ShouldCloseLegacyRealm() {
        Realm.getInstance(config).use { saveFakePerson(it, getFakePerson()) }

        RealmDbManagerImpl(testContext, TestProjectIdProvider(), TestLocalDbKeyProvider())
            .signInToLocal()
            .test()
            .awaitAndAssertSuccess()
            .let {
                assert(Realm.getInstance(config).use { it.isClosed })
            }
    }

    @Test
    fun localSignInWithEncryptionChange_ShouldHaveValidData() {
        val fakePerson = getFakePerson()
        Realm.getInstance(legacyConfig).use { saveFakePerson(it, fakePerson) }

        RealmDbManagerImpl(testContext, TestProjectIdProvider(), TestLocalDbKeyProvider())
            .signInToLocal()
            .test()
            .awaitAndAssertSuccess()
            .let {
                Realm.getInstance(config).use {
                    val newPerson = it.where(rl_Person::class.java).findFirst()
                    Assert.assertNotNull(newPerson)
                    Assert.assertTrue(newPerson!!.deepEquals(fakePerson))
                }
            }
    }

    @After
    fun cleanUp() {
        deleteRealmFiles(legacyConfig)
        deleteRealmFiles(config)
    }

}
