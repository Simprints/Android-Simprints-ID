package com.simprints.id.services.scheduledSync.peopleDownSync

import com.google.firebase.FirebaseApp
import com.google.gson.stream.JsonReader
import com.nhaarman.mockito_kotlin.*
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.data.db.local.realm.models.rl_SyncInfo
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.data.db.sync.room.SyncStatusDao
import com.simprints.id.data.db.sync.room.SyncStatusDatabase
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.Constants
import com.simprints.id.network.SimApiClient
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.shared.PeopleGeneratorUtils
import com.simprints.id.shared.PeopleGeneratorUtils.getRandomPeople
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.whenever
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.mockServer.assertPathUrlParam
import com.simprints.id.testUtils.mockServer.assertQueryUrlParam
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.json.JsonHelper
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@Suppress("UNCHECKED_CAST")
@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class PeopleDownSyncTaskTest : RxJavaTest {

    private var mockServer = MockWebServer()
    private lateinit var apiClient: SimApiClient<PeopleRemoteInterface>
    private lateinit var remotePeopleApi: PeopleRemoteInterface
    private lateinit var syncStatusDatabaseModel: SyncStatusDao


    val dbManager: DbManager = mock()
    private val remoteDbManagerSpy: RemoteDbManager = spy()
    private val syncStatusDatabase: SyncStatusDatabase = mock()
    private val loginInfoManager: LoginInfoManager = mock()
    val preferencesManager: PreferencesManager = mock()

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        dbManager.initialiseDb()

        whenever(remoteDbManagerSpy.getCurrentFirestoreToken()).thenReturn(Single.just(""))

        mockServer.start()
        setupApi()
    }

    @Test
    fun downloadPatientsForGlobalSync_shouldSuccess() {
        val localDbMock = Mockito.mock(LocalDbManager::class.java)

        //Params
        val projectIdTest = "projectIDTest"
        val syncParams = SyncTaskParameters.GlobalSyncTaskParameters(projectIdTest)
        val nPeopleToDownload = 22000
        val peopleToDownload = getRandomPeople(nPeopleToDownload).map { fb_Person(it) }.sortedBy { it.updatedAt }

        makeFakeDownloadRequest(peopleToDownload, localDbMock, 3000, syncParams)

        val peopleRequestUrl = mockServer.takeRequest().requestUrl
        assertPathUrlParam(peopleRequestUrl, projectIdTest)
        val lastDownloadedPatient = peopleToDownload.last()
        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientUpdatedAt", lastDownloadedPatient.updatedAt!!.time) { it?.toLong() }
        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientId", lastDownloadedPatient.patientId)
    }

    @Test
    fun downloadPatientsForModuleSync_shouldSuccess() {
        val localDbMock = Mockito.mock(LocalDbManager::class.java)

        //Params
        val projectIdTest = "projectIdTest"
        val moduleIdTest = "moduleIdTest"
        val syncParams = SyncTaskParameters.ModuleIdSyncTaskParameters(projectIdTest, moduleIdTest)
        val nPeopleToDownload = 22000
        val peopleToDownload = getRandomPeople(nPeopleToDownload).map { fb_Person(it) }.sortedBy { it.updatedAt }

        makeFakeDownloadRequest(peopleToDownload, localDbMock, 3000, syncParams)

        val peopleRequestUrl = mockServer.takeRequest().requestUrl
        assertPathUrlParam(peopleRequestUrl, projectIdTest)
        assertQueryUrlParam(peopleRequestUrl, "moduleId", moduleIdTest)
        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientUpdatedAt", peopleToDownload.last().updatedAt!!.time) { it?.toLong() }
        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientId", peopleToDownload.last().patientId)
    }

    @Test
    fun downloadPatientsForUserSync_shouldSuccess() {
        val localDbMock = Mockito.mock(LocalDbManager::class.java)

        //Params
        val projectIdTest = "projectIdTest"
        val userIdTest = "userIdTest"
        val syncParams = SyncTaskParameters.UserSyncTaskParameters(projectIdTest, userIdTest)
        val nPeopleToDownload = 22000
        val peopleToDownload = getRandomPeople(nPeopleToDownload).map { fb_Person(it) }.sortedBy { it.updatedAt }

        makeFakeDownloadRequest(peopleToDownload, localDbMock, 3000, syncParams)

        val peopleRequestUrl = mockServer.takeRequest().requestUrl
        assertPathUrlParam(peopleRequestUrl, projectIdTest)
        assertQueryUrlParam(peopleRequestUrl, "userId", userIdTest)
        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientUpdatedAt", peopleToDownload.last().updatedAt!!.time) { it?.toLong() }
        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientId", peopleToDownload.last().patientId)
    }

    private fun setupApi() {
        PeopleRemoteInterface.baseUrl = this.mockServer.url("/").toString()
        apiClient = SimApiClient(PeopleRemoteInterface::class.java, PeopleRemoteInterface.baseUrl)
        remotePeopleApi = apiClient.api
    }

    private fun makeFakeDownloadRequest(
        peopleToDownload: List<fb_Person>,
        localDbMock: LocalDbManager,
        patientsAlreadyInLocalDb: Int,
        syncParams: SyncTaskParameters
    ) {

        //Build fake response for GET patients
        val patientsToDownload = ArrayList(peopleToDownload)

        //Mock GET patients
        mockServer.enqueue(mockResponseForDownloadPatients(patientsToDownload))

        //Mock saving patient in Realm
        mockLocalDbToSavePatientsFromStream(localDbMock)

        //Mock app has already patients in localDb
        whenever(localDbMock.getPeopleCountFromLocal(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull())).thenReturn(Single.just(patientsAlreadyInLocalDb))

        //Mock app RealmSyncInfo for syncParams
        whenever(localDbMock.getSyncInfoFor(anyNotNull())).thenReturn(Single.create { it.onSuccess(rl_SyncInfo(syncParams.toGroup(), rl_Person(peopleToDownload.last()))) })

        // Mock when trying to save the syncInfo
        whenever(localDbMock.updateSyncInfo(anyNotNull())).thenReturn(Completable.complete())

        whenever(preferencesManager.syncGroup).thenReturn(Constants.GROUP.GLOBAL)
        whenever(preferencesManager.moduleId).thenReturn("")
        whenever(loginInfoManager.getSignedInUserIdOrEmpty()).thenReturn("")
        whenever(loginInfoManager.getSignedInProjectIdOrEmpty()).thenReturn("")
        whenever(dbManager.remote).thenReturn(remoteDbManagerSpy)
        whenever(dbManager.remote.getPeopleApiClient()).thenReturn(Single.just(remotePeopleApi))

        whenever(syncStatusDatabase.syncStatusModel).thenReturn(mock())
        syncStatusDatabaseModel = syncStatusDatabase.syncStatusModel
        whenever(syncStatusDatabaseModel.getPeopleToDownSync()).doReturn(25000)
        doNothing().whenever(syncStatusDatabaseModel).updateLastDownSyncTime(any())
        doNothing().whenever(syncStatusDatabaseModel).updatePeopleToDownSyncCount(any())

        val sync = PeopleDownSyncTask(remoteDbManagerSpy, dbManager, preferencesManager, loginInfoManager, localDbMock, syncStatusDatabaseModel)
        sync.syncParams = syncParams
        sync.execute()
    }

    private fun mockLocalDbToSavePatientsFromStream(localDbMock: LocalDbManager) {
        whenever(localDbMock.savePeopleFromStreamAndUpdateSyncInfo(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull())).thenAnswer { invocation ->
            Completable.create {
                val args = invocation.arguments
                (args[0] as JsonReader).skipValue()
                val shouldStop = args[3] as (savedPerson: fb_Person) -> Boolean
                if (shouldStop(fb_Person(PeopleGeneratorUtils.getRandomPerson()))) {
                    it.onComplete()
                }
            }
        }
    }

    private fun mockResponseForDownloadPatients(patients: ArrayList<fb_Person>): MockResponse? {
        val fbPersonJson = JsonHelper.gson.toJson(patients)
        return MockResponse().let {
            it.setResponseCode(200)
            it.setBody(fbPersonJson)
        }
    }

    @After
    @Throws
    fun tearDown() {
        mockServer.shutdown()
    }
}
