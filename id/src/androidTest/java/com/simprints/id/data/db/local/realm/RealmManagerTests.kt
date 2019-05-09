package com.simprints.id.data.db.local.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.commontesttools.DefaultTestConstants
import com.simprints.id.commontesttools.PeopleGeneratorUtils.getRandomPeople
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.models.DbPerson
import com.simprints.id.data.db.local.realm.models.toDomainPerson
import com.simprints.id.data.db.local.realm.models.toRealmPerson
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
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
        realmManager = RealmDbManagerImpl(testContext, secureDataManagerMock, loginInfoManagerMock)
    }

    @Test
    fun changeLocalDbKey_shouldNotAllowedToUseFirstRealm() {
        saveFakePerson(realm, getFakePerson())
        val countNewRealm = realmManager.getPeopleCountFromLocal().blockingGet()
        assertEquals(countNewRealm, 1)

        val differentNewDatabaseName = "different_${Date().time}newDatabase"
        val differentDatabaseKey: ByteArray = "different_newKey".toByteArray().copyOf(KEY_LENGTH)
        val differentSecureDataManagerMock = mock<SecureDataManager>().apply {
            whenever(this) { getLocalDbKeyOrThrow(DefaultTestConstants.DEFAULT_PROJECT_ID) }
                .thenReturn(LocalDbKey(differentNewDatabaseName, differentDatabaseKey))
        }
        val differentRealmManager = RealmDbManagerImpl(testContext, differentSecureDataManagerMock, loginInfoManagerMock)

        val count = differentRealmManager.getPeopleCountFromLocal().blockingGet()
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

        val count = realmManager.getPeopleCountFromLocal(projectId = fakePerson.patientId).blockingGet()
        assertEquals(count, 1)
    }

    @Test
    fun insertOrUpdatePerson_ShouldSucceed() {
        val fakePerson = getFakePerson()
        realmManager.insertOrUpdatePersonInLocal(fakePerson.toDomainPerson()).blockingAwait()

        assertEquals(realm.where(DbPerson::class.java).count(), 1)
        assertTrue(realm.where(DbPerson::class.java).findFirst()!!.deepEquals(fakePerson))
    }

    @Test
    fun insertOrUpdateSamePerson_ShouldNotSaveTwoPeople() {
        val fakePerson = getFakePerson()
        realmManager.insertOrUpdatePersonInLocal(fakePerson.toDomainPerson()).blockingAwait()
        realmManager.insertOrUpdatePersonInLocal(fakePerson.toDomainPerson()).blockingAwait()

        assertEquals(realm.where(DbPerson::class.java).count(), 1)
        assertTrue(realm.where(DbPerson::class.java).findFirst()!!.deepEquals(fakePerson))
    }

    @Test
    fun loadPeopleFromLocal_ShouldSucceed() {
        val fakePerson = getFakePerson()
        saveFakePerson(realm, fakePerson)

        val people = realmManager.loadPeopleFromLocal().blockingGet()
        listOf(fakePerson).zip(people).forEach { assertTrue(it.first.deepEquals(it.second.toRealmPerson())) }
    }

    @Test
    fun loadPeopleFromLocalByUserId_ShouldLoadOnlyUsersPeople() {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val people = realmManager.loadPeopleFromLocal(userId = fakePerson.userId).blockingGet()
        listOf(fakePerson).zip(people).forEach { assertTrue(it.first.deepEquals(it.second.toRealmPerson())) }
    }

    @Test
    fun loadPeopleFromLocalByModuleId_ShouldLoadOnlyModulesPeople() {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val people = realmManager.loadPeopleFromLocal(moduleId = fakePerson.moduleId).blockingGet()
        listOf(fakePerson).zip(people).forEach { assertTrue(it.first.deepEquals(it.second.toRealmPerson())) }
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
