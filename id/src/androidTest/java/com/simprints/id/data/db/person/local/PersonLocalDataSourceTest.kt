package com.simprints.id.data.db.person.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.commontesttools.DefaultTestConstants
import com.simprints.id.commontesttools.PeopleGeneratorUtils.getRandomPeople
import com.simprints.id.data.db.person.local.models.DbPerson
import com.simprints.id.data.db.person.local.models.toDomainPerson
import com.simprints.id.data.db.person.local.models.toRealmPerson
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.LocalDbKey
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import io.realm.Realm
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class PersonLocalDataSourceTest : RealmTestsBase() {

    private lateinit var realm: Realm
    private lateinit var personLocalDataSource: PersonLocalDataSource

    private val loginInfoManagerMock = mock<LoginInfoManager>().apply {
        whenever(this) { getSignedInProjectIdOrEmpty() }
            .thenReturn(DefaultTestConstants.DEFAULT_PROJECT_ID)
    }
    private val secureDataManagerMock = mock<SecureDataManager>().apply {
        whenever(this) { getLocalDbKeyOrThrow(DefaultTestConstants.DEFAULT_PROJECT_ID) }
            .thenReturn(LocalDbKey(newDatabaseName, newDatabaseKey))
    }

    @Before
    fun setup() {
        realm = Realm.getInstance(config)
        personLocalDataSource = PersonLocalDataSourceImpl(testContext, secureDataManagerMock, loginInfoManagerMock)
    }

    @Test
    fun changeLocalDbKey_shouldNotAllowedToUseFirstRealm() {
        saveFakePerson(realm, getFakePerson())
        val countNewRealm = runBlocking { personLocalDataSource.count() }
        assertEquals(countNewRealm, 1)

        val differentNewDatabaseName = "different_${Date().time}newDatabase"
        val differentDatabaseKey: ByteArray = "different_newKey".toByteArray().copyOf(KEY_LENGTH)
        val differentSecureDataManagerMock = mock<SecureDataManager>().apply {
            whenever(this) { getLocalDbKeyOrThrow(DefaultTestConstants.DEFAULT_PROJECT_ID) }
                .thenReturn(LocalDbKey(differentNewDatabaseName, differentDatabaseKey))
        }
        val differentRealmManager = PersonLocalDataSourceImpl(testContext, differentSecureDataManagerMock, loginInfoManagerMock)

        val count = runBlocking { differentRealmManager.count() }
        assertEquals(count, 0)
    }

    @Test
    fun getPeopleCountFromLocal_ShouldReturnOne() {
        saveFakePerson(realm, getFakePerson())

        val count = runBlocking { personLocalDataSource.count() }
        assertEquals(count, 1)
    }

    @Test
    fun getPeopleCountFromLocal_ShouldReturnMany() {
        saveFakePeople(realm, getRandomPeople(20))

        val count = runBlocking { personLocalDataSource.count() }
        assertEquals(count, 20)
    }

    @Test
    fun getPeopleCountFromLocalByUserId_ShouldReturnOne() {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val count = runBlocking { personLocalDataSource.count(PersonLocalDataSource.Query(userId = fakePerson.userId)) }
        assertEquals(count, 1)
    }

    @Test
    fun getPeopleCountFromLocalByModuleId_ShouldReturnOne() {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val count = personLocalDataSource.count(PersonLocalDataSource.Query(moduleId = fakePerson.moduleId))
        assertEquals(count, 1)
    }

    @Test
    fun getPeopleCountFromLocalByProjectId_ShouldReturnAll() {
        saveFakePeople(realm, getRandomPeople(20))

        val count = runBlocking { personLocalDataSource.count() }
        assertEquals(count, 20)
    }

    @Test
    fun getPeopleCountFromLocalByPatientId_ShouldReturnOne() {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val count = runBlocking { personLocalDataSource.count(PersonLocalDataSource.Query(patientId = fakePerson.patientId)) }
        assertEquals(count, 1)
    }

    @Test
    fun insertOrUpdatePerson_ShouldSucceed() = runBlocking {
        val fakePerson = getFakePerson()
        personLocalDataSource.insertOrUpdate(listOf(fakePerson.toDomainPerson()))

        realm.executeTransaction {
            assertEquals(realm.where(DbPerson::class.java).count(), 1)
            assertTrue(realm.where(DbPerson::class.java).findFirst()!!.deepEquals(fakePerson))
        }
    }

    @Test
    fun insertOrUpdateSamePerson_ShouldNotSaveTwoPeople() = runBlocking {
        val fakePerson = getFakePerson()
        personLocalDataSource.insertOrUpdate(listOf(fakePerson.toDomainPerson()))
        personLocalDataSource.insertOrUpdate(listOf(fakePerson.toDomainPerson()))

        realm.executeTransaction {
            assertEquals(realm.where(DbPerson::class.java).count(), 1)
            assertTrue(realm.where(DbPerson::class.java).findFirst()!!.deepEquals(fakePerson))
        }
    }

    @Test
    fun loadPeopleFromLocal_ShouldSucceed() {
        val fakePerson = getFakePerson()
        saveFakePerson(realm, fakePerson)

        val people = runBlocking { personLocalDataSource.load().toList() }

        listOf(fakePerson).zip(people).forEach { assertTrue(it.first.deepEquals(it.second.toRealmPerson())) }
    }

    @Test
    fun loadPeopleFromLocalByUserId_ShouldLoadOnlyUsersPeople() {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val people = runBlocking { personLocalDataSource.load(PersonLocalDataSource.Query(userId = fakePerson.userId)).toList() }
        listOf(fakePerson).zip(people).forEach { assertTrue(it.first.deepEquals(it.second.toRealmPerson())) }
    }

    @Test
    fun loadPeopleFromLocalByModuleId_ShouldLoadOnlyModulesPeople() {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val people = runBlocking { personLocalDataSource.load(PersonLocalDataSource.Query(moduleId = fakePerson.moduleId)).toList() }
        listOf(fakePerson).zip(people).forEach { assertTrue(it.first.deepEquals(it.second.toRealmPerson())) }
    }

    @Test
    fun loadPeopleFromLocalByToSyncTrue_ShouldLoadAllPeople() {
        saveFakePeople(realm, getRandomPeople(20, toSync = true))

        val people = runBlocking { personLocalDataSource.load(PersonLocalDataSource.Query(toSync = true)).toList() }
        assertEquals(people.size, 20)
    }

    @Test
    fun loadPeopleFromLocalByToSyncFalse_ShouldLoadNoPeople() {
        saveFakePeople(realm, getRandomPeople(20, toSync = true))

        val people = runBlocking { personLocalDataSource.load(PersonLocalDataSource.Query(toSync = false)).toList() }
        assertEquals(people.size, 0)
    }
}
