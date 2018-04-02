package com.simprints.id.data.db

import com.nhaarman.mockito_kotlin.argumentCaptor
import com.simprints.id.BuildConfig
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.network.RemoteApiInterface
import com.simprints.id.network.SimApiClient
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
import java.util.*
import java.util.concurrent.CompletableFuture

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApplication::class)
class DbManagerTest : RxJavaTest() {

    private var mockServer = MockWebServer()
    private lateinit var apiClient: SimApiClient<RemoteApiInterface>

    @Before
    fun setUp() {
        mockServer.start()
        apiClient = SimApiClient(RemoteApiInterface::class.java, RemoteApiInterface.baseUrl)
    }

    @Test
    fun savingPerson_shouldSaveThenUpdatePersonLocally() {
        val (dbManager, localDbManager, _) = getDbManagerWithMockedLocalAndRemoteManagersForApiTesting(mockServer)
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
        Thread.sleep(4000)

        val argument = argumentCaptor<rl_Person>()
        verify(localDbManager, times(2)).insertOrUpdatePersonInLocal(argument.capture())

        // First time we save the person in the local db, it doesn't have times and it needs to be sync
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
        val fakePerson = fb_Person(PeopleGeneratorUtils.getRandomPerson().apply {
            updatedAt = null
            createdAt = null
        })

        for (i in 0..20) mockServer.enqueue(mockFailingResponse())

        val testObservable = dbManager.savePerson(fakePerson).test()

        testObservable.awaitTerminalEvent()

        val argument = argumentCaptor<rl_Person>()
        verify(localDbManager, times(1)).insertOrUpdatePersonInLocal(argument.capture())

        Assert.assertNull(argument.firstValue.createdAt)
        Assert.assertNull(argument.firstValue.updatedAt)
        Assert.assertTrue(argument.firstValue.toSync)
    }

    @After
    @Throws
    fun tearDown() {
        mockServer.shutdown()
    }
}
