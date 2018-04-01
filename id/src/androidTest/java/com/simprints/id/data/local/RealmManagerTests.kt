package com.simprints.id.data.local

import android.support.test.runner.AndroidJUnit4
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.local.RealmDbManager
import com.simprints.id.data.db.local.RealmDbManager.Companion.PATIENT_ID_FIELD
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.domain.Constants
import com.simprints.id.tools.extensions.awaitAndAssertSuccess
import com.simprints.id.tools.json.JsonHelper
import com.simprints.id.tools.utils.PeopleGeneratorUtils.getRandomPeople
import io.realm.Realm
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStreamReader
import java.io.Reader


@RunWith(AndroidJUnit4::class)
class RealmManagerTests : RealmTestsBase() {

    private lateinit var realm: Realm
    private lateinit var realmManager: RealmDbManager

    @Before
    fun setup() {
        realm = Realm.getInstance(config)
        realmManager = RealmDbManager(testContext).apply {
            signInToLocal(localDbKey).blockingAwait()
        }
    }

    @Test
    fun signInToLocal_ShouldSucceed() {
        realmManager.signInToLocal(localDbKey).test().awaitAndAssertSuccess()
    }

    @Test
    fun getPersonsCountFromLocal_ShouldReturnOne() {
        saveFakePerson(realm, getFakePerson())

        val count = realmManager.getPersonsCountFromLocal()
        assertEquals(count, 1)
    }

    @Test
    fun insertOrUpdatePerson_ShouldSucceed() {
        val fakePerson = getFakePerson()
        realmManager.insertOrUpdatePersonInLocal(fakePerson)

        assertEquals(realm.where(rl_Person::class.java).count(), 1)
        assertTrue(realm.where(rl_Person::class.java).findFirst()!!.deepEquals(fakePerson))
    }

    @Test
    fun loadPersonsFromLocal_ShouldSucceed() {
        val fakePerson = getFakePerson()
        saveFakePerson(realm, fakePerson)

        val people = realmManager.loadPersonsFromLocal()
        assertTrue(people.first().deepEquals(fakePerson))
    }

    @Test
    fun loadPersonsFromLocalByUserId_ShouldLoadOnlyUsersPeople() {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val people = realmManager.loadPersonsFromLocal(userId = fakePerson.userId)
        assertTrue(people.first().deepEquals(fakePerson))
        assertEquals(people.size, 1)
    }

    @Test
    fun loadPersonsFromLocalByModuleId_ShouldLoadOnlyModulesPeople() {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val people = realmManager.loadPersonsFromLocal(moduleId = fakePerson.moduleId)
        assertTrue(people.first().deepEquals(fakePerson))
        assertEquals(people.size, 1)
    }

    @Test
    fun savePeopleFromStream_ShouldSucceed() {
        val downloadPeople = getRandomPeople(35)
        val json = JsonHelper.toJson(downloadPeople.map { fb_Person(it) }).byteInputStream()
        val reader = JsonReader(InputStreamReader(json) as Reader?).apply { beginArray() }

        realmManager.savePeopleFromStream(reader, Gson(), Constants.GROUP.GLOBAL, { false })

        assertEquals(realm.where(rl_Person::class.java).count(), 35)
        downloadPeople.forEach {
            assertTrue(realm.where(rl_Person::class.java).contains(PATIENT_ID_FIELD, it.patientId).isValid)
        }
    }

    @After
    fun cleanUp() {
        deleteAll(realm)
        realm.close()
    }

}
