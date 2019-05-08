package com.simprints.id.data.db

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.simprints.core.network.SimApiClient
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.commontesttools.PeopleGeneratorUtils
import com.simprints.testtools.common.di.DependencyRule.*
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.data.analytics.eventdata.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.models.toDomainPerson
import com.simprints.id.data.db.local.realm.models.toRealmPerson
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.models.ApiPerson
import com.simprints.id.data.db.remote.models.toDomainPerson
import com.simprints.id.data.db.remote.models.toApiPerson
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.data.db.remote.people.RemotePeopleManager
import com.simprints.id.domain.Person
import com.simprints.id.exceptions.unexpected.DownloadingAPersonWhoDoesntExistOnServerException
import com.simprints.id.services.scheduledSync.PeopleApiServiceMock
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.testtools.state.RobolectricTestMocker.setupLocalAndRemoteManagersForApiTesting
import com.simprints.testtools.common.retrofit.createMockBehaviorService
import com.simprints.testtools.common.syntax.spy
import com.simprints.testtools.common.syntax.verifyOnce
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.unit.mockserver.mockNotFoundResponse
import com.simprints.testtools.unit.mockserver.mockServerProblemResponse
import com.simprints.testtools.unit.mockserver.mockSuccessfulResponse
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.reactivex.Single
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.IOException
import java.util.*
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class DbManagerTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private var mockServer = MockWebServer()
    private lateinit var apiClient: SimApiClient<PeopleRemoteInterface>

    @Inject lateinit var localDbManagerSpy: LocalDbManager
    @Inject lateinit var remoteDbManagerSpy: RemoteDbManager
    @Inject lateinit var remotePeopleManagerSpy: RemotePeopleManager
    @Inject lateinit var sessionEventsLocalDbManagerSpy: SessionEventsLocalDbManager
    @Inject lateinit var peopleUpSyncMasterMock: PeopleUpSyncMaster
    @Inject lateinit var dbManager: DbManager

    private val module by lazy {
        TestAppModule(
            app,
            localDbManagerRule = ReplaceRule { spy<LocalDbManager>() },
            remoteDbManagerRule = SpyRule,
            remotePeopleManagerRule = SpyRule,
            peopleUpSyncMasterRule = MockRule,
            sessionEventsLocalDbManagerRule = MockRule
        )
    }

    @Before
    fun setUp() {
        UnitTestConfig(this, module).fullSetup()

        mockServer.start()
        apiClient = SimApiClient(PeopleRemoteInterface::class.java, PeopleRemoteInterface.baseUrl)

        setupLocalAndRemoteManagersForApiTesting(localDbManagerSpy, remoteDbManagerSpy, sessionEventsLocalDbManagerSpy, mockServer)
    }

    @Test
    fun savingPerson_shouldSaveThenScheduleUpSync() {
        val fakePerson = PeopleGeneratorUtils.getRandomPerson().toRealmPerson().apply {
            updatedAt = null
            createdAt = null
        }.toDomainPerson().toApiPerson()

        mockServer.enqueue(mockSuccessfulResponse())
        mockServer.enqueue(mockResponseForDownloadPatient(fakePerson.copy().apply {
            updatedAt = Date(1)
            createdAt = Date(0)
        }))

        val testObservable = dbManager.savePerson(fakePerson.toDomainPerson()).test()

        testObservable.awaitTerminalEvent()
        testObservable
            .assertNoErrors()
            .assertComplete()

        // savePerson makes an async task in the OnComplete, we need to wait it finishes.
        Thread.sleep(1000)

        val argument = argumentCaptor<Person>()
        verifyOnce(localDbManagerSpy) { insertOrUpdatePersonInLocal(argument.capture()) }

        // First time we save the person in the local dbManager, it doesn't have times and it needs to be sync
        Assert.assertNull(argument.firstValue.createdAt)
        Assert.assertNull(argument.firstValue.updatedAt)
        Assert.assertTrue(argument.firstValue.toSync)

        verifyOnce(peopleUpSyncMasterMock) { schedule(fakePerson.projectId/*, fakePerson.userId*/) } // TODO: uncomment userId when multitenancy is properly implemented
    }

    @Test
    fun loadingPersonMissingInLocalDb_shouldStillLoadFromRemoteDb() {
        val person = PeopleGeneratorUtils.getRandomPerson()

        mockServer.enqueue(mockResponseForDownloadPatient(person.toApiPerson()))

        val testObserver = dbManager.loadPerson(person.projectId, person.patientId).test()

        testObserver.awaitTerminalEvent()
        testObserver.assertNoErrors()
            .assertValue { personFetchResult -> personFetchResult.fetchedOnline }

        verifyOnce(remotePeopleManagerSpy) { downloadPerson(person.patientId, person.projectId) }
    }

    @Test
    fun savingPerson_serverProblemStillSavesPerson() {
        val fakePerson = PeopleGeneratorUtils.getRandomPerson().toRealmPerson().apply {
            updatedAt = null
            createdAt = null
        }.toDomainPerson()

        for (i in 0..20) mockServer.enqueue(mockServerProblemResponse())

        val testObservable = dbManager.savePerson(fakePerson).test()

        testObservable.awaitTerminalEvent()

        val argument = argumentCaptor<Person>()
        verifyOnce(localDbManagerSpy) { insertOrUpdatePersonInLocal(argument.capture()) }

        Assert.assertNull(argument.firstValue.createdAt)
        Assert.assertNull(argument.firstValue.updatedAt)
        Assert.assertTrue(argument.firstValue.toSync)
    }

    @Test
    fun savingPerson_noConnectionStillSavesPerson() {
        val fakePerson = PeopleGeneratorUtils.getRandomPerson().toRealmPerson().apply {
            updatedAt = null
            createdAt = null
        }.toDomainPerson()

        val poorNetworkClientMock: PeopleRemoteInterface = PeopleApiServiceMock(createMockBehaviorService(apiClient.retrofit, 100, PeopleRemoteInterface::class.java))
        whenever(remotePeopleManagerSpy) { getPeopleApiClient() } thenReturn Single.just(poorNetworkClientMock)

        val testObservable = dbManager.savePerson(fakePerson).test()

        testObservable.awaitTerminalEvent()
        testObservable.assertNoErrors()

        val argument = argumentCaptor<Person>()
        verifyOnce(localDbManagerSpy) { insertOrUpdatePersonInLocal(argument.capture()) }

        Assert.assertNull(argument.firstValue.createdAt)
        Assert.assertNull(argument.firstValue.updatedAt)
        Assert.assertTrue(argument.firstValue.toSync)
    }

    @Test
    fun loadingPersonMissingInLocalAndRemoteDbs_shouldTriggerDataError() {
        val person = PeopleGeneratorUtils.getRandomPerson()

        mockServer.enqueue(mockNotFoundResponse())

        val testObserver = dbManager.loadPerson(person.projectId, person.patientId).test()

        testObserver.awaitTerminalEvent()
        testObserver.assertError(DownloadingAPersonWhoDoesntExistOnServerException::class.java)

        verifyOnce(remotePeopleManagerSpy) { downloadPerson(person.patientId, person.projectId) }
    }

    @Test
    fun loadingPersonMissingInLocalAndWithNoConnection_shouldTriggerDataError() {
        val person = PeopleGeneratorUtils.getRandomPerson()

        val poorNetworkClientMock: PeopleRemoteInterface = PeopleApiServiceMock(createMockBehaviorService(apiClient.retrofit, 100, PeopleRemoteInterface::class.java))
        whenever(remotePeopleManagerSpy) { getPeopleApiClient() } thenReturn Single.just(poorNetworkClientMock)

        val testObserver = dbManager.loadPerson(person.projectId, person.patientId).test()

        testObserver.awaitTerminalEvent()
        testObserver.assertError(IOException::class.java)

        verifyOnce(remotePeopleManagerSpy) { downloadPerson(person.patientId, person.projectId) }
    }

    @After
    @Throws
    fun tearDown() {
        mockServer.shutdown()
    }

    private fun mockResponseForDownloadPatient(patient: ApiPerson): MockResponse {
        return MockResponse().let {
            it.setResponseCode(200)
            it.setBody(JsonHelper.toJson(patient))
        }
    }
}
