package com.simprints.id.data.local

import android.support.test.runner.AndroidJUnit4
import com.simprints.id.data.db.local.RealmDbManager
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.id.tools.extensions.awaitAndAssertSuccess
import io.realm.Realm
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class RealmManagerTests : RealmTestsBase() {

    private lateinit var realm: Realm
    private lateinit var realmManager: RealmDbManager

    @Before
    fun setup() {
        realm = Realm.getInstance(config)
        realmManager = RealmDbManager(testContext)
    }

    @Test
    fun signInToLocal_ShouldSucceed() {
        realmManager.signInToLocal(localDbKey).test().awaitAndAssertSuccess()
    }

    @Test
    fun getPersonsCountFromLocal_ShouldReturnOne() {
        saveFakePerson(realm, getFakePerson())
        realmManager.signInToLocal(localDbKey).blockingAwait()

        val count = realmManager.getPersonsCountFromLocal()
        assertEquals(count, 1)
    }

    @Test
    fun insertOrUpdatePerson_ShouldSucceed() {
        realmManager.signInToLocal(localDbKey).blockingAwait()

        val fakePerson = getFakePerson()
        realmManager.insertOrUpdatePersonInLocal(fakePerson)

        assertEquals(realm.where(rl_Person::class.java).count(), 1)
        assertTrue(realm.where(rl_Person::class.java).findFirst()!!.deepEquals(fakePerson))
    }

    @Test
    fun loadPersonsFromLocal_ShouldSucceed() {
        val fakePerson = getFakePerson()
        saveFakePerson(realm, fakePerson)

        realmManager.signInToLocal(localDbKey).blockingAwait()

        val people = realmManager.loadPersonsFromLocal()
        assertTrue(people.first().deepEquals(fakePerson))
    }

    @After
    fun cleanUp() {
        deleteAll(realm)
        realm.close()
    }

}
