package com.simprints.id.data.local

import androidx.test.runner.AndroidJUnit4
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.RealmDbManagerImpl
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.data.db.local.realm.models.toDomainPerson
import com.simprints.id.commontesttools.PeopleGeneratorUtils.getRandomPeople
import io.realm.Realm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class RealmManagerTests : RealmTestsBase() {

    private lateinit var realm: Realm
    private lateinit var realmManager: RealmDbManagerImpl

    @Before
    fun setup() {
        realm = Realm.getInstance(config)
        realmManager = RealmDbManagerImpl(testContext)
        realmManager.signInToLocal(LocalDbKey(newDatabaseName, newDatabaseKey, legacyDatabaseName))
    }

    @Test
    fun changeLocalDbKey_shouldNotAllowedToUseFirstRealm() {
        saveFakePerson(realm, getFakePerson())
        val countNewRealm = realmManager.getPeopleCountFromLocal().blockingGet()
        assertEquals(countNewRealm, 1)

        val differentNewDatabaseName = "different_${Date().time}newDatabase"
        val differentDatabaseKey: ByteArray = Arrays.copyOf("different_newKey".toByteArray(), KEY_LENGTH)
        val differentLegacyDatabaseName = "different_${Date().time}legacyDB"
        realmManager.signInToLocal(LocalDbKey(differentNewDatabaseName, differentDatabaseKey, differentLegacyDatabaseName))
        val count = realmManager.getPeopleCountFromLocal().blockingGet()
        assertEquals(count, 0)
    }

    @Test
    fun getPeopleCountFromLocal_ShouldReturnOne() {
        saveFakePerson(realm, getFakePerson())

        val count = realmManager.getPeopleCountFromLocal().blockingGet()
        assertEquals(count, 1)
    }

    @Test
    fun getPeopleCountFromLocal_ShouldReturnMany() {
        saveFakePeople(realm, getRandomPeople(20))

        val count = realmManager.getPeopleCountFromLocal().blockingGet()
        assertEquals(count, 20)
    }

    @Test
    fun getPeopleCountFromLocalByUserId_ShouldReturnOne() {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val count = realmManager.getPeopleCountFromLocal(userId = fakePerson.userId).blockingGet()
        assertEquals(count, 1)
    }

    @Test
    fun getPeopleCountFromLocalByModuleId_ShouldReturnOne() {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val count = realmManager.getPeopleCountFromLocal(moduleId = fakePerson.moduleId).blockingGet()
        assertEquals(count, 1)
    }

    @Test
    fun getPeopleCountFromLocalByProjectId_ShouldReturnAll() {
        saveFakePeople(realm, getRandomPeople(20))

        val count = realmManager.getPeopleCountFromLocal().blockingGet()
        assertEquals(count, 20)
    }

    @Test
    fun getPeopleCountFromLocalByPatientId_ShouldReturnOne() {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val count = realmManager.getPeopleCountFromLocal(patientId = fakePerson.patientId).blockingGet()
        assertEquals(count, 1)
    }

    @Test
    fun insertOrUpdatePerson_ShouldSucceed() {
        val fakePerson = getFakePerson()
        realmManager.insertOrUpdatePersonInLocal(fakePerson).blockingAwait()

        assertEquals(realm.where(rl_Person::class.java).count(), 1)
        assertTrue(realm.where(rl_Person::class.java).findFirst()!!.deepEquals(fakePerson))
    }

    @Test
    fun insertOrUpdateSamePerson_ShouldNotSaveTwoPeople() {
        val fakePerson = getFakePerson()
        realmManager.insertOrUpdatePersonInLocal(fakePerson).blockingAwait()
        realmManager.insertOrUpdatePersonInLocal(fakePerson).blockingAwait()

        assertEquals(realm.where(rl_Person::class.java).count(), 1)
        assertTrue(realm.where(rl_Person::class.java).findFirst()!!.deepEquals(fakePerson))
    }

    @Test
    fun loadPeopleFromLocal_ShouldSucceed() {
        val fakePerson = getFakePerson()
        saveFakePerson(realm, fakePerson)

        val people = realmManager.loadPeopleFromLocal().blockingGet()
        assertEquals(listOf(fakePerson.toDomainPerson()), people)
    }

    @Test
    fun loadPeopleFromLocalByUserId_ShouldLoadOnlyUsersPeople() {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val people = realmManager.loadPeopleFromLocal(userId = fakePerson.userId).blockingGet()
        assertEquals(listOf(fakePerson.toDomainPerson()), people)
    }

    @Test
    fun loadPeopleFromLocalByModuleId_ShouldLoadOnlyModulesPeople() {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val people = realmManager.loadPeopleFromLocal(moduleId = fakePerson.moduleId).blockingGet()
        assertEquals(listOf(fakePerson.toDomainPerson()), people)
    }

    @Test
    fun loadPeopleFromLocalByToSyncTrue_ShouldLoadAllPeople() {
        saveFakePeople(realm, getRandomPeople(20, toSync = true))

        val people = realmManager.loadPeopleFromLocal(toSync = true).blockingGet()
        assertEquals(people.size, 20)
    }

    @Test
    fun loadPeopleFromLocalByToSyncFalse_ShouldLoadNoPeople() {
        saveFakePeople(realm, getRandomPeople(20, toSync = true))

        val people = realmManager.loadPeopleFromLocal(toSync = false).blockingGet()
        assertEquals(people.size, 0)
    }
}
