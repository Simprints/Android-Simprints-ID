package com.simprints.id.data.local

import android.support.test.runner.AndroidJUnit4
import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.RealmDbManagerImpl
import com.simprints.id.data.db.local.realm.RealmDbManagerImpl.Companion.SYNC_ID_FIELD
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.data.db.local.realm.models.rl_SyncInfo
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.domain.Constants
import com.simprints.id.domain.Constants.GROUP.*
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.testTools.extensions.awaitAndAssertSuccess
import com.simprints.id.tools.json.JsonHelper
import com.simprints.id.shared.PeopleGeneratorUtils.getRandomPeople
import io.reactivex.Completable
import io.realm.Realm
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStreamReader
import java.io.Reader
import java.util.*
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class RealmManagerTests : RealmTestsBase() {

    companion object {
        const val FAKE_DB_FIELD = "infoField"
    }

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
        assertTrue(people.first().deepEquals(fakePerson))
    }

    @Test
    fun loadPeopleFromLocalByUserId_ShouldLoadOnlyUsersPeople() {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val people = realmManager.loadPeopleFromLocal(userId = fakePerson.userId).blockingGet()
        assertTrue(people.first().deepEquals(fakePerson))
        assertEquals(people.size, 1)
    }

    @Test
    fun loadPeopleFromLocalByModuleId_ShouldLoadOnlyModulesPeople() {
        val fakePerson = saveFakePerson(realm, getFakePerson())
        saveFakePeople(realm, getRandomPeople(20))

        val people = realmManager.loadPeopleFromLocal(moduleId = fakePerson.moduleId).blockingGet()
        assertTrue(people.first().deepEquals(fakePerson))
        assertEquals(people.size, 1)
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

    @Test
    fun savePeopleFromStream_ShouldSucceed() {
        val downloadPeople = getRandomPeople(35)
        saveFromStream(GLOBAL, 35, downloadPeople).test().awaitAndAssertSuccess()

        assertEquals(realm.where(rl_Person::class.java).count(), 35)
        downloadPeople.forEach {
            assertTrue(realm.where(rl_Person::class.java).contains(rl_Person.PATIENT_ID_FIELD, it.patientId).isValid)
        }
    }

    @Test
    fun savePeopleFromStream_ShouldSaveLatestSyncTime() {
        val downloadPeople = getRandomPeople(35)
        saveFromStream(GLOBAL, 35, downloadPeople).test().awaitAndAssertSuccess()

        val latestPersonTime = Calendar.getInstance().apply {
            time = downloadPeople.maxBy { it.updatedAt?.time ?: 0 }?.updatedAt
        }

        val dbSyncTime = Calendar.getInstance().apply {
            time = realm.where(rl_SyncInfo::class.java)
                .equalTo(SYNC_ID_FIELD, GLOBAL.ordinal)
                .findAll()
                .first()!!.lastSyncTime
        }

        val diff = abs(latestPersonTime.get(Calendar.SECOND) - dbSyncTime.get(Calendar.SECOND))
        assertTrue(diff == 0 || diff == 1)
    }

    @Test
    fun updateSyncInfo_ShouldSucceed() {
        saveFromStream(GLOBAL).test().awaitAndAssertSuccess()

        assertEquals(realm.where(rl_SyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, GLOBAL.ordinal).count(), 1)
        assertEquals(realm.where(rl_SyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, GLOBAL.ordinal).findFirst()!!.syncGroupId, GLOBAL.ordinal)
    }

    @Test
    fun updateSyncInfoBesidesProject_ShouldNotReturnProjectSync() {
        saveFromStream(USER).test().awaitAndAssertSuccess()
        saveFromStream(MODULE).test().awaitAndAssertSuccess()

        assertEquals(realm.where(rl_SyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, GLOBAL.ordinal).count(), 0)
        assertEquals(realm.where(rl_SyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, GLOBAL.ordinal).findFirst(), null)
    }

    @Test
    fun updateUserSyncInfo_ShouldSucceed() {
        saveFromStream(USER).test().awaitAndAssertSuccess()

        assertEquals(realm.where(rl_SyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, USER.ordinal).count(), 1)
        assertEquals(realm.where(rl_SyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, USER.ordinal).findFirst()!!.syncGroupId, USER.ordinal)
    }

    @Test
    fun updateSyncInfoBesidesUser_ShouldNotReturnUserSync() {
        saveFromStream(GLOBAL).test().awaitAndAssertSuccess()
        saveFromStream(MODULE).test().awaitAndAssertSuccess()

        assertEquals(realm.where(rl_SyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, USER.ordinal).count(), 0)
        assertEquals(realm.where(rl_SyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, USER.ordinal).findFirst(), null)
    }

    @Test
    fun updateModuleSyncInfo_ShouldSucceed() {
        saveFromStream(MODULE).test().awaitAndAssertSuccess()

        assertEquals(realm.where(rl_SyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, MODULE.ordinal).count(), 1)
        assertEquals(realm.where(rl_SyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, MODULE.ordinal).findFirst()!!.syncGroupId, MODULE.ordinal)
    }

    @Test
    fun updateSyncInfoBesidesModule_ShouldNotReturnModuleSync() {
        saveFromStream(GLOBAL).test().awaitAndAssertSuccess()
        saveFromStream(USER).test().awaitAndAssertSuccess()

        assertEquals(realm.where(rl_SyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, MODULE.ordinal).count(), 0)
        assertEquals(realm.where(rl_SyncInfo::class.java)
            .equalTo(SYNC_ID_FIELD, MODULE.ordinal).findFirst(), null)
    }

    @Test
    fun getSyncInfoForGlobal_ShouldSucceed() {
        val fakeSync = saveFakeSyncInfo(realm)
        val loadSyncInfo = realmManager.getSyncInfoFor(GLOBAL).blockingGet()

        assertTrue(loadSyncInfo!!.deepEquals(fakeSync))
    }

    @Test
    fun getSyncInfoForUser_ShouldSucceed() {
        val fakeSync = saveFakeSyncInfo(realm, userId = FAKE_DB_FIELD)
        val loadSyncInfo = realmManager.getSyncInfoFor(USER).blockingGet()

        assertTrue(loadSyncInfo!!.deepEquals(fakeSync))
    }

    @Test
    fun getSyncInfoForModule_ShouldSucceed() {
        val fakeSync = saveFakeSyncInfo(realm, moduleId = FAKE_DB_FIELD)
        val loadSyncInfo = realmManager.getSyncInfoFor(MODULE).blockingGet()

        assertTrue(loadSyncInfo!!.deepEquals(fakeSync))
    }

    @After
    fun cleanUp() {
        deleteAll(realm)
        realm.close()
    }

    private fun saveFromStream(group: Constants.GROUP,
                               numberOfPeople: Int = 35,
                               downloadPeople: ArrayList<rl_Person> = getRandomPeople(numberOfPeople)): Completable {

        val json = JsonHelper.toJson(downloadPeople.map { fb_Person(it) }).byteInputStream()
        val reader = JsonReader(InputStreamReader(json) as Reader?).apply { beginArray() }

        val taskParams = when (group) {
            Constants.GROUP.GLOBAL -> SyncTaskParameters.GlobalSyncTaskParameters(
                projectId = downloadPeople.first().projectId
            )
            Constants.GROUP.USER -> SyncTaskParameters.UserSyncTaskParameters(
                projectId = downloadPeople.first().projectId,
                userId = downloadPeople.first().userId
            )
            Constants.GROUP.MODULE -> SyncTaskParameters.ModuleIdSyncTaskParameters(
                projectId = downloadPeople.first().projectId,
                moduleId = downloadPeople.first().moduleId
            )
        }

        return realmManager.savePeopleFromStreamAndUpdateSyncInfo(reader, JsonHelper.gson, taskParams, { false })
    }
}
