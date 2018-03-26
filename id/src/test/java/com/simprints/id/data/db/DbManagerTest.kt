package com.simprints.id.data.db

import com.simprints.id.BuildConfig
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.sync.SyncApiInterface
import com.simprints.id.network.SimApiClient
import com.simprints.id.testUtils.anyNotNull
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.retrofit.mockServer.mockFailingResponse
import com.simprints.id.testUtils.retrofit.mockServer.mockResponseForDownloadPatient
import com.simprints.id.testUtils.retrofit.mockServer.mockResponseForUploadPatient
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.testUtils.roboletric.getDbManagerWithMockedLocalAndRemoteManagersForApiTesting
import com.simprints.id.tools.utils.PeopleGeneratorUtils
import com.simprints.libcommon.Person
import junit.framework.Assert
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.CompletableFuture

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApplication::class)
class DbManagerTest : RxJavaTest() {

    private var mockServer = MockWebServer()
    private lateinit var apiClient: SimApiClient<SyncApiInterface>

    @Before
    fun setUp() {
        mockServer.start()
        apiClient = SimApiClient(SyncApiInterface::class.java, SyncApiInterface.baseUrl)
    }

    @Test
    fun savingPerson_shouldSaveThenUpdatePersonLocally() {
        val (dbManager, localDbManager, _) = getDbManagerWithMockedLocalAndRemoteManagersForApiTesting(mockServer)
        val fakePerson = fb_Person(PeopleGeneratorUtils.getRandomPerson())

        mockServer.enqueue(mockResponseForUploadPatient())
        mockServer.enqueue(mockResponseForDownloadPatient(fakePerson))

        val testObservable = dbManager.savePerson(fakePerson).test()

        testObservable.awaitTerminalEvent()
        testObservable
            .assertNoErrors()
            .assertComplete()

        verify(localDbManager, times(2)).insertOrUpdatePersonInLocal(anyNotNull())
    }

    @Test
    fun loadingPersonMissingInLocalDb_shouldStillLoadFromRemoteDb() {
        val (dbManager, _, dbRemoteManager) = getDbManagerWithMockedLocalAndRemoteManagersForApiTesting(mockServer)

        val person = PeopleGeneratorUtils.getRandomPerson()

        mockServer.enqueue(mockResponseForDownloadPatient(fb_Person(person)))

        val result = mutableListOf<Person>()

        val futureResultIsNotEmpty = CompletableFuture<Boolean>()
        val callback = object : DataCallback {
            override fun onSuccess() {
                futureResultIsNotEmpty.complete(result.isEmpty())
            }

            override fun onFailure(data_error: DATA_ERROR) {
            }
        }

        dbManager.loadPerson(result, person.projectId, person.patientId, callback = callback)

        Assert.assertFalse(futureResultIsNotEmpty.get())
        verify(dbRemoteManager, times(1)).downloadPerson(person.patientId, person.projectId)
    }

    @Test
    fun savingPerson_serverProblemStillSavesPerson() {
        val (dbManager, localDbManager, _) = getDbManagerWithMockedLocalAndRemoteManagersForApiTesting(mockServer)
        val fakePerson = fb_Person(PeopleGeneratorUtils.getRandomPerson())

        for (i in 0..20) mockServer.enqueue(mockFailingResponse())

        val testObservable = dbManager.savePerson(fakePerson).test()

        testObservable.awaitTerminalEvent()
        testObservable.assertError(Throwable::class.java)

        verify(localDbManager, times(1)).insertOrUpdatePersonInLocal(anyNotNull())
    }

    @After
    @Throws
    fun tearDown() {
        mockServer.shutdown()
    }
}
