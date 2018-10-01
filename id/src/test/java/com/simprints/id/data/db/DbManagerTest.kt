package com.simprints.id.data.db

import com.google.firebase.FirebaseApp
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.simprints.id.data.analytics.eventData.SessionEventsLocalDbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.data.db.remote.people.RemotePeopleManager
import com.simprints.id.di.AppModuleForTests
import com.simprints.id.di.DaggerForTests
import com.simprints.id.network.SimApiClient
import com.simprints.id.shared.DependencyRule.*
import com.simprints.id.shared.PeopleGeneratorUtils
import com.simprints.id.shared.createMockBehaviorService
import com.simprints.id.shared.whenever
import com.simprints.id.sync.SimApiMock
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.retrofit.mockServer.mockNotFoundResponse
import com.simprints.id.testUtils.retrofit.mockServer.mockResponseForDownloadPatient
import com.simprints.id.testUtils.retrofit.mockServer.mockResponseForUploadPatient
import com.simprints.id.testUtils.retrofit.mockServer.mockServerProblemResponse
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.testUtils.roboletric.setupLocalAndRemoteManagersForApiTesting
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.libcommon.Person
import io.reactivex.Single
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class DbManagerTest : RxJavaTest, DaggerForTests() {

    private var mockServer = MockWebServer()
    private lateinit var apiClient: SimApiClient<PeopleRemoteInterface>

    @Inject lateinit var localDbManagerSpy: LocalDbManager
    @Inject lateinit var remoteDbManagerSpy: RemoteDbManager
    @Inject lateinit var remotePeopleManagerSpy: RemotePeopleManager
    @Inject lateinit var sessionEventsLocalDbManagerSpy: SessionEventsLocalDbManager
    @Inject lateinit var dbManager: DbManager

    override var module by lazyVar {
        AppModuleForTests(
            app,
            localDbManagerRule = ReplaceRule { spy(LocalDbManager::class.java) },
            remoteDbManagerRule = SpyRule,
            sessionEventsLocalDbManagerRule = MockRule
        )
    }

    @Before
    override fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as TestApplication)
        super.setUp()
        testAppComponent.inject(this)

        mockServer.start()
        apiClient = SimApiClient(PeopleRemoteInterface::class.java, PeopleRemoteInterface.baseUrl)

        setupLocalAndRemoteManagersForApiTesting(mockServer, localDbManagerSpy, remoteDbManagerSpy, sessionEventsLocalDbManagerSpy)
    }

    @Test
    fun savingPerson_shouldSaveThenUpdatePersonLocally() {
        val fakePerson = fb_Person(PeopleGeneratorUtils.getRandomPerson().apply {
            updatedAt = null
            createdAt = null
        })

        mockServer.enqueue(mockResponseForUploadPatient())
        mockServer.enqueue(mockResponseForDownloadPatient(fakePerson.copy().apply {
            updatedAt = Date(1)
            createdAt = Date(0)
        }))

        val testObservable = dbManager.savePerson(fakePerson).test()

        testObservable.awaitTerminalEvent()
        testObservable
            .assertNoErrors()
            .assertComplete()

        // savePerson makes an async task in the OnComplete, we need to wait it finishes.
        Thread.sleep(1000)

        val argument = argumentCaptor<rl_Person>()
        verify(localDbManagerSpy, times(2)).insertOrUpdatePersonInLocal(argument.capture())

        // First time we save the person in the local dbManager, it doesn't have times and it needs to be sync
        Assert.assertNull(argument.firstValue.createdAt)
        Assert.assertNull(argument.firstValue.updatedAt)
        Assert.assertTrue(argument.firstValue.toSync)

        // Second time, it's after we download the person from our server, with timestamp.
        Assert.assertNotNull(argument.secondValue.createdAt)
        Assert.assertNotNull(argument.secondValue.updatedAt)
        Assert.assertFalse(argument.secondValue.toSync)
    }

    @Test
    fun loadingPersonMissingInLocalDb_shouldStillLoadFromRemoteDb() {
        val person = PeopleGeneratorUtils.getRandomPerson()

        mockServer.enqueue(mockResponseForDownloadPatient(fb_Person(person)))

        val result = mutableListOf<Person>()

        val futureResultIsNotEmpty = CompletableFuture<Boolean>()
        val callback = object : DataCallback {
            override fun onSuccess(isDataFromRemote: Boolean) {
                futureResultIsNotEmpty.complete(result.isEmpty())
            }

            override fun onFailure(data_error: DATA_ERROR) {
            }
        }

        dbManager.loadPerson(result, person.projectId, person.patientId, callback = callback)

        Assert.assertFalse(futureResultIsNotEmpty.get())
        verify(remotePeopleManagerSpy, times(1)).downloadPerson(person.patientId, person.projectId)
    }

    @Test
    fun savingPerson_serverProblemStillSavesPerson() {
        val fakePerson = fb_Person(PeopleGeneratorUtils.getRandomPerson().apply {
            updatedAt = null
            createdAt = null
        })

        for (i in 0..20) mockServer.enqueue(mockServerProblemResponse())

        val testObservable = dbManager.savePerson(fakePerson).test()

        testObservable.awaitTerminalEvent()

        val argument = argumentCaptor<rl_Person>()
        verify(localDbManagerSpy, times(1)).insertOrUpdatePersonInLocal(argument.capture())

        Assert.assertNull(argument.firstValue.createdAt)
        Assert.assertNull(argument.firstValue.updatedAt)
        Assert.assertTrue(argument.firstValue.toSync)
    }

    @Test
    fun savingPerson_noConnectionStillSavesPerson() {
        val fakePerson = fb_Person(PeopleGeneratorUtils.getRandomPerson().apply {
            updatedAt = null
            createdAt = null
        })

        val poorNetworkClientMock: PeopleRemoteInterface = SimApiMock(createMockBehaviorService(apiClient.retrofit, 100, PeopleRemoteInterface::class.java))
        whenever(remotePeopleManagerSpy.getPeopleApiClient()).thenReturn(Single.just(poorNetworkClientMock))

        val testObservable = dbManager.savePerson(fakePerson).test()

        testObservable.awaitTerminalEvent()
        testObservable.assertNoErrors()

        val argument = argumentCaptor<rl_Person>()
        verify(localDbManagerSpy, times(1)).insertOrUpdatePersonInLocal(argument.capture())

        Assert.assertNull(argument.firstValue.createdAt)
        Assert.assertNull(argument.firstValue.updatedAt)
        Assert.assertTrue(argument.firstValue.toSync)
    }

    @Test
    fun loadingPersonMissingInLocalAndRemoteDbs_shouldTriggerDataError() {
        val person = PeopleGeneratorUtils.getRandomPerson()

        mockServer.enqueue(mockNotFoundResponse())

        val result = mutableListOf<Person>()

        val futurePersonExists = CompletableFuture<Boolean>()
        val futureDataErrorExistsAndIsPersonNotFound = CompletableFuture<Boolean>()
        val callback = object : DataCallback {
            override fun onSuccess(isDataFromRemote: Boolean) {
                futurePersonExists.complete(true)
            }

            override fun onFailure(data_error: DATA_ERROR) {
                futurePersonExists.complete(false)
                futureDataErrorExistsAndIsPersonNotFound.complete(data_error == DATA_ERROR.NOT_FOUND)
            }
        }

        dbManager.loadPerson(result, person.projectId, person.patientId, callback = callback)

        Assert.assertFalse(futurePersonExists.get())
        Assert.assertTrue(futureDataErrorExistsAndIsPersonNotFound.get())
        verify(remotePeopleManagerSpy, times(1)).downloadPerson(person.patientId, person.projectId)
    }

    @Test
    fun loadingPersonMissingInLocalAndWithNoConnection_shouldTriggerDataError() {
        val person = PeopleGeneratorUtils.getRandomPerson()

        val poorNetworkClientMock: PeopleRemoteInterface = SimApiMock(createMockBehaviorService(apiClient.retrofit, 100, PeopleRemoteInterface::class.java))
        whenever(remotePeopleManagerSpy.getPeopleApiClient()).thenReturn(Single.just(poorNetworkClientMock))

        val result = mutableListOf<Person>()

        val futurePersonExists = CompletableFuture<Boolean>()
        val futureDataErrorExistsAndIsPersonNotFound = CompletableFuture<Boolean>()
        val callback = object : DataCallback {
            override fun onSuccess(isDataFromRemote: Boolean) {
                futurePersonExists.complete(true)
            }

            override fun onFailure(data_error: DATA_ERROR) {
                futurePersonExists.complete(false)
                futureDataErrorExistsAndIsPersonNotFound.complete(data_error == DATA_ERROR.NOT_FOUND)
            }
        }

        dbManager.loadPerson(result, person.projectId, person.patientId, callback = callback)

        Assert.assertFalse(futurePersonExists.get())
        Assert.assertTrue(futureDataErrorExistsAndIsPersonNotFound.get())
        verify(remotePeopleManagerSpy, times(1)).downloadPerson(person.patientId, person.projectId)
    }

    @After
    @Throws
    fun tearDown() {
        mockServer.shutdown()
    }
}
