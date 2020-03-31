package com.simprints.id.services.people

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.network.SimApiClientFactory
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.Application
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.commontesttools.AndroidDefaultTestConstants.DEFAULT_LOCAL_DB_KEY
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.DefaultTestConstants.moduleSyncScope
import com.simprints.id.commontesttools.DefaultTestConstants.projectSyncScope
import com.simprints.id.commontesttools.DefaultTestConstants.userSyncScope
import com.simprints.id.commontesttools.PeopleGeneratorUtils.getRandomPeople
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestDataModule
import com.simprints.id.commontesttools.di.TestSyncModule
import com.simprints.id.data.db.common.models.PeopleCount
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncScope
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PeopleRemoteInterface
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.person.remote.models.ApiGetPerson
import com.simprints.id.data.db.person.remote.models.fromDomainToApi
import com.simprints.id.data.db.person.remote.models.peopleoperations.response.ApiPeopleOperationCounts
import com.simprints.id.data.db.person.remote.models.peopleoperations.response.ApiPeopleOperationGroupResponse
import com.simprints.id.data.db.person.remote.models.peopleoperations.response.ApiPeopleOperationsResponse
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.LegacyLocalDbKeyProvider
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncState
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncWorkerState.*
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.testtools.android.runOnActivity
import com.simprints.testtools.common.di.DependencyRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.withTimeout
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject


@RunWith(AndroidJUnit4::class)
class PeopleSyncIntegrationTest {

    companion object {
        const val N_TO_DOWNLOAD_PER_MODULE = 100
        const val N_TO_UPLOAD = 10
    }

    private var mockServer = MockWebServer()
    private var mockDispatcher = MockDispatcher()

    private val app: Application = ApplicationProvider.getApplicationContext()

    @Inject lateinit var personRemoteDataSourceSpy: PersonRemoteDataSource
    @Inject lateinit var personLocalDataSourceSpy: PersonLocalDataSource
    @Inject lateinit var downSyncScopeRepositorySpy: PeopleDownSyncScopeRepository
    @Inject lateinit var peopleSyncManager: PeopleSyncManager

    @MockK lateinit var loginInfoManagerMock: LoginInfoManager
    @MockK lateinit var secureLocalDbKeyProviderMock: SecureLocalDbKeyProvider
    @MockK lateinit var legacyLocalDbKeyProviderMock: LegacyLocalDbKeyProvider

    private val appModule by lazy {
        TestAppModule(
            app,
            legacyLocalDbKeyProviderRule = DependencyRule.ReplaceRule { legacyLocalDbKeyProviderMock },
            secureDataManagerRule = DependencyRule.ReplaceRule { secureLocalDbKeyProviderMock },
            loginInfoManagerRule = DependencyRule.ReplaceRule { loginInfoManagerMock })
    }

    private val dataModule by lazy {
        TestDataModule(
            personRemoteDataSourceRule = DependencyRule.SpykRule,
            personLocalDataSourceRule = DependencyRule.SpykRule)
    }

    private val syncModule by lazy {
        TestSyncModule(peopleDownSyncScopeRepositoryRule = DependencyRule.SpykRule)
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        AndroidTestConfig(this, appModule = appModule, dataModule = dataModule, syncModule = syncModule).fullSetup()

        mockServer.start()
        mockServer.dispatcher = mockDispatcher

        val remotePeopleApi = SimApiClientFactory("deviceId").build<PeopleRemoteInterface>(
            mockServer.url("/").toString()
        ).api

        coEvery { personRemoteDataSourceSpy.getPeopleApiClient() } returns remotePeopleApi
        every { downSyncScopeRepositorySpy.getDownSyncScope() } returns projectSyncScope
        every { secureLocalDbKeyProviderMock.getLocalDbKeyOrThrow(any()) } returns DEFAULT_LOCAL_DB_KEY
        every { legacyLocalDbKeyProviderMock.getLocalDbKeyOrThrow(any()) } returns DEFAULT_LOCAL_DB_KEY
        every { loginInfoManagerMock.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
        every { loginInfoManagerMock.getSignedInUserIdOrEmpty() } returns DEFAULT_USER_ID

    }

    @Test
    fun syncByProjectSuccessfully() = runBlockingTest {
        runSyncTest { activity ->
            mockUploadPeople()
            val total = mockResponsesForSync(projectSyncScope)

            runAndVerifySyncSucceeds(activity, total + N_TO_UPLOAD)
        }
    }

    @Test
    fun syncByUserSuccessfully() = runBlockingTest {
        runSyncTest { activity ->
            val total = mockResponsesForSync(userSyncScope)
            runAndVerifySyncSucceeds(activity, total)
        }
    }


    @Test
    fun syncByModuleSuccessfully() = runBlockingTest {
        runSyncTest { activity ->
            val total = mockResponsesForSync(moduleSyncScope)
            runAndVerifySyncSucceeds(activity, total)
        }
    }

    @Test
    fun uploadFailsBecauseANotCloudIssue_shouldSyncRetry() = runBlockingTest {
        runSyncTest { activity ->
            mockResponsesForSync(projectSyncScope)
            mockUploadPeople()
            mockDispatcher.uploadResponse = 300 to ""

            runAndVerifySyncRetries(activity)
        }
    }

    @Test
    fun uploadFailsBecauseACloudIssue_shouldSyncFail() = runBlockingTest {
        runSyncTest { activity ->
            mockResponsesForSync(projectSyncScope)
            mockUploadPeople()
            mockDispatcher.uploadResponse = 505 to ""

            runAndVerifySyncFails(activity)
        }
    }

    @Test
    fun downloadFailsBecauseACloudIssue_shouldSyncRetry() = runBlockingTest {
        runSyncTest { activity ->
            mockResponsesForSync(projectSyncScope)
            mockDispatcher.downResponse = 300 to listOf()

            runAndVerifySyncRetries(activity)
        }
    }

    @Test
    fun downloadFailsBecauseACloudIssue_shouldSyncFail() = runBlockingTest {
        runSyncTest { activity ->
            mockResponsesForSync(projectSyncScope)
            mockDispatcher.downResponse = 505 to listOf()

            runAndVerifySyncFails(activity)
        }
    }

    @Test
    fun downCountFailsBecauseANotCloudIssue_shouldSyncSucceed() = runBlockingTest {
        runSyncTest { activity ->
            mockResponsesForSync(projectSyncScope)
            mockDispatcher.countResponse = 300 to null

            runAndVerifySyncRetries(activity)
        }
    }

    @Test
    fun downCountFailsBecauseANotCloudIssue_shouldSyncRetry() = runBlockingTest {
        runSyncTest { activity ->
            mockResponsesForSync(projectSyncScope)
            mockDispatcher.countResponse = 300 to null

            runAndVerifySyncRetries(activity)
        }
    }


    private fun runAndVerifySyncSucceeds(act: Activity, total: Int) {
        peopleSyncManager.getLastSyncState().observe(act as LifecycleOwner, Observer {
            if (!(it.anySyncWorkersStillRunning() || it.anySyncWorkersEnqueued())) {
                it.assertSyncSucceeds(total)
            }
        })

        peopleSyncManager.sync()
    }

    private fun runAndVerifySyncFails(act: Activity) {
        peopleSyncManager.getLastSyncState().observe(act as LifecycleOwner, Observer {
            if (!(it.anySyncWorkersStillRunning() || it.anySyncWorkersEnqueued())) {
                it.assertSyncFails()
            }
        })

        peopleSyncManager.sync()
    }

    private fun runAndVerifySyncRetries(act: Activity) {
        peopleSyncManager.getLastSyncState().observe(act as LifecycleOwner, Observer {
            if (!it.anySyncWorkersStillRunning()) {
                it.assertSyncRetries()
            }
        })

        peopleSyncManager.sync()
    }


    private suspend fun runSyncTest(timeout: Long = 10000, block: (act: Activity) -> Unit) =
        withTimeout(timeout) {
            runOnActivity<RequestLoginActivity> {
                block(it)
            }
        }


private fun mockResponsesForSync(scope: PeopleDownSyncScope): Int {
    val ops = runBlocking { downSyncScopeRepositorySpy.getDownSyncOperations(scope) }
    val apiPeopleToDownload = ops.map {
        val peopleToDownload = getRandomPeople(N_TO_DOWNLOAD_PER_MODULE, it, listOf(false))
        peopleToDownload.map { it.fromDomainToGetApi() }.sortedBy { it.updatedAt }
    }.flatten()

    val countResponse = ApiPeopleOperationsResponse(listOf(PeopleCount(apiPeopleToDownload.size, 0, 0)).map {
        ApiPeopleOperationGroupResponse(ApiPeopleOperationCounts(it.created, it.deleted, it.updated))
    })

    mockDispatcher.downResponse = 200 to apiPeopleToDownload
    mockDispatcher.countResponse = 200 to countResponse

    return apiPeopleToDownload.size
}


private fun mockUploadPeople() {
    val ops = runBlocking { downSyncScopeRepositorySpy.getDownSyncOperations(projectSyncScope) }
    coEvery { personLocalDataSourceSpy.load(any()) } returns getRandomPeople(N_TO_UPLOAD, ops.first(), listOf(true)).asFlow()
}
}

class MockDispatcher : Dispatcher() {

    var countResponse: Pair<Int, ApiPeopleOperationsResponse?>? = null
    var downResponse: Pair<Int, List<ApiGetPerson>?>? = null
    var uploadResponse: Pair<Int, String>? = null

    override fun dispatch(request: RecordedRequest): MockResponse {
        val lastPart = request.requestUrl?.pathSegments?.last()

        return if (lastPart == "patients" && request.method == "POST") {
            val code = uploadResponse?.first ?: 200
            MockResponse().setResponseCode(code)
        } else if (lastPart == "count") {
            val code = countResponse?.first ?: 200
            val response = JsonHelper.gson.toJson(countResponse?.second ?: "")
            MockResponse().setResponseCode(code).setBody(response)
        } else if (lastPart == "patients" && request.method == "GET") {
            val code = downResponse?.first ?: 200
            val response = JsonHelper.gson.toJson(downResponse?.second ?: "")
            MockResponse().setResponseCode(code).setBody(response)
        } else {
            MockResponse().setResponseCode(404)
        }
    }
}

private fun PeopleSyncState.anySyncWorkersEnqueued(): Boolean =
    downSyncWorkersInfo.plus(upSyncWorkersInfo).any { it.state is Enqueued }

private fun PeopleSyncState.anySyncWorkersStillRunning(): Boolean =
    downSyncWorkersInfo.plus(upSyncWorkersInfo).any { it.state is Running }

private fun PeopleSyncState.assertSyncRetries() {
    assertThat((downSyncWorkersInfo.plus(upSyncWorkersInfo)).any { it.state is Enqueued }).isTrue()
}

private fun PeopleSyncState.assertSyncFails() {
    assertThat((downSyncWorkersInfo.plus(upSyncWorkersInfo)).any { it.state is Failed }).isTrue()
}


private fun PeopleSyncState.assertSyncSucceeds(total: Int) {
    assertThat(total).isEqualTo(total)
    assertThat(progress).isEqualTo(total)
    assertThat((downSyncWorkersInfo.plus(upSyncWorkersInfo)).all { it.state is Succeeded }).isTrue()
}

fun Person.fromDomainToGetApi(deleted: Boolean = false): ApiGetPerson =
    ApiGetPerson(
        id = patientId,
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        fingerprints = fingerprintSamples.map { it.fromDomainToApi() },
        deleted = deleted
    )
