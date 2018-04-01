package com.simprints.id.data.local

import android.support.test.runner.AndroidJUnit4
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.local.RealmDbManager
import com.simprints.id.data.db.local.RealmDbManager.Companion.PATIENT_ID_FIELD
import com.simprints.id.data.db.local.RealmDbManager.Companion.SYNC_ID_FIELD
import com.simprints.id.data.db.local.RealmSyncInfo
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.domain.Constants.GROUP.*
import com.simprints.id.services.sync.SyncTaskParameters.*
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

    companion object {
        const val SYNC_INFO_FIELD = "infoField"
    }

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
    fun getPersonsCountFromLocal_ShouldReturnMany() {
        saveFakePeople(realm, getRandomPeople(20))

        val count = realmManager.getPersonsCountFromLocal()
        assertEquals(count, 20)
    }

    @Test
    fun getPersonsCountFromLocalByUserId_ShouldReturnOne() {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val count = realmManager.getPersonsCountFromLocal(userId = fakePerson.userId)
        assertEquals(count, 1)
    }

    @Test
    fun getPersonsCountFromLocalByModuleId_ShouldReturnOne() {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val count = realmManager.getPersonsCountFromLocal(moduleId = fakePerson.moduleId)
        assertEquals(count, 1)
    }

    @Test
    fun getPersonsCountFromLocalByProjectId_ShouldReturnOne() {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val count = realmManager.getPersonsCountFromLocal(projectId = fakePerson.projectId)
        assertEquals(count, 1)
    }

    @Test
    fun getPersonsCountFromLocalByPatientId_ShouldReturnOne() {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val count = realmManager.getPersonsCountFromLocal(patientId = fakePerson.patientId)
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
    fun loadPersonsFromLocalByProjectId_ShouldLoadOnlyProjectsPeople() {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val people = realmManager.loadPersonsFromLocal(projectId = fakePerson.projectId)
        assertTrue(people.first().deepEquals(fakePerson))
        assertEquals(people.size, 1)
    }

    @Test
    fun loadPersonsFromLocalByToSyncTrue_ShouldLoadAllPeople() {
        saveFakePeople(realm, getRandomPeople(20))

        val people = realmManager.loadPersonsFromLocal(toSync = true)
        assertEquals(people.size, 20)
    }

    @Test
    fun loadPersonsFromLocalByToSyncFalse_ShouldLoadNoPeople() {
        saveFakePeople(realm, getRandomPeople(20))

        val people = realmManager.loadPersonsFromLocal(toSync = false)
        assertEquals(people.size, 0)
    }

    @Test
    fun savePeopleFromStream_ShouldSucceed() {
        val downloadPeople = getRandomPeople(35)
        val json = JsonHelper.toJson(downloadPeople.map { fb_Person(it) }).byteInputStream()
        val reader = JsonReader(InputStreamReader(json) as Reader?).apply { beginArray() }

        realmManager.savePeopleFromStream(reader, Gson(), GLOBAL, { false })

        assertEquals(realm.where(rl_Person::class.java).count(), 35)
        downloadPeople.forEach {
            assertTrue(realm.where(rl_Person::class.java).contains(PATIENT_ID_FIELD, it.patientId).isValid)
        }
    }

    @Test
    fun updateSyncInfo_ShouldSucceed() {
        realmManager.updateSyncInfo(GlobalSyncTaskParameters(SYNC_INFO_FIELD))

        assertEquals(realm.where(RealmSyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, GLOBAL.ordinal).count(), 1)
        assertEquals(realm.where(RealmSyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, GLOBAL.ordinal).findFirst()!!.id, GLOBAL.ordinal)
    }

    @Test
    fun updateSyncInfoBesidesProject_ShouldNotReturnProjectSync() {
        realmManager.updateSyncInfo(UserSyncTaskParameters(SYNC_INFO_FIELD, SYNC_INFO_FIELD))
        realmManager.updateSyncInfo(ModuleIdSyncTaskParameters(SYNC_INFO_FIELD, SYNC_INFO_FIELD))

        assertEquals(realm.where(RealmSyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, GLOBAL.ordinal).count(), 0)
        assertEquals(realm.where(RealmSyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, GLOBAL.ordinal).findFirst(), null)
    }

    @Test
    fun updateUserSyncInfo_ShouldSucceed() {
        realmManager.updateSyncInfo(UserSyncTaskParameters(SYNC_INFO_FIELD, SYNC_INFO_FIELD))

        assertEquals(realm.where(RealmSyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, USER.ordinal).count(), 1)
        assertEquals(realm.where(RealmSyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, USER.ordinal).findFirst()!!.id, USER.ordinal)
    }

    @Test
    fun updateSyncInfoBesidesUser_ShouldNotReturnUserSync() {
        realmManager.updateSyncInfo(GlobalSyncTaskParameters(SYNC_INFO_FIELD))
        realmManager.updateSyncInfo(ModuleIdSyncTaskParameters(SYNC_INFO_FIELD, SYNC_INFO_FIELD))

        assertEquals(realm.where(RealmSyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, USER.ordinal).count(), 0)
        assertEquals(realm.where(RealmSyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, USER.ordinal).findFirst(), null)
    }

    @Test
    fun updateModuleSyncInfo_ShouldSucceed() {
        realmManager.updateSyncInfo(ModuleIdSyncTaskParameters(SYNC_INFO_FIELD, SYNC_INFO_FIELD))

        assertEquals(realm.where(RealmSyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, MODULE.ordinal).count(), 1)
        assertEquals(realm.where(RealmSyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, MODULE.ordinal).findFirst()!!.id, MODULE.ordinal)
    }

    @Test
    fun updateSyncInfoBesidesModule_ShouldNotReturnModuleSync() {
        realmManager.updateSyncInfo(GlobalSyncTaskParameters(SYNC_INFO_FIELD))
        realmManager.updateSyncInfo(UserSyncTaskParameters(SYNC_INFO_FIELD, SYNC_INFO_FIELD))

        assertEquals(realm.where(RealmSyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, MODULE.ordinal).count(), 0)
        assertEquals(realm.where(RealmSyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, MODULE.ordinal).findFirst(), null)
    }

    @Test
    fun getSyncInfoForGlobal_ShouldSucceed() {
        val fakeSync = saveFakeSyncInfo(realm)
        val loadSyncInfo = realmManager.getSyncInfoFor(GLOBAL)

        assertTrue(loadSyncInfo!!.deepEquals(fakeSync))
    }

    @Test
    fun getSyncInfoForUser_ShouldSucceed() {
        val fakeSync = saveFakeSyncInfo(realm, userId = SYNC_INFO_FIELD)
        val loadSyncInfo = realmManager.getSyncInfoFor(USER)

        assertTrue(loadSyncInfo!!.deepEquals(fakeSync))
    }

    @Test
    fun getSyncInfoForModule_ShouldSucceed() {
        val fakeSync = saveFakeSyncInfo(realm, moduleId = SYNC_INFO_FIELD)
        val loadSyncInfo = realmManager.getSyncInfoFor(MODULE)

        assertTrue(loadSyncInfo!!.deepEquals(fakeSync))
    }

    @After
    fun cleanUp() {
        deleteAll(realm)
        realm.close()
    }

}
