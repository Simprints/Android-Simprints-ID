package com.simprints.id.services.people

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.work.WorkInfo
import com.google.common.truth.Truth.assertThat
import com.simprints.core.network.SimApiClient
import com.simprints.core.tools.extentions.resumeSafely
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
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncState
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.testtools.android.runOnActivity
import com.simprints.testtools.common.di.DependencyRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
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
@SmallTest
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
    @Inject lateinit var loginInfoManagerMock: LoginInfoManager
    @Inject lateinit var downSyncScopeRepositorySpy: PeopleDownSyncScopeRepository
    @Inject lateinit var secureLocalDbKeyProviderMock: SecureLocalDbKeyProvider
    @Inject lateinit var peopleSyncManager: PeopleSyncManager

    private val appModule by lazy {
        TestAppModule(
            app,
            secureDataManagerRule = DependencyRule.MockkRule,
            loginInfoManagerRule = DependencyRule.MockkRule)
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
        AndroidTestConfig(this, appModule = appModule, dataModule = dataModule, syncModule = syncModule).fullSetup()
        MockKAnnotations.init(this, relaxUnitFun = true)

        mockServer.start()
        mockServer.dispatcher = mockDispatcher

        PeopleRemoteInterface.baseUrl = this.mockServer.url("/").toString()
        val remotePeopleApi: PeopleRemoteInterface = SimApiClient(PeopleRemoteInterface::class.java, PeopleRemoteInterface.baseUrl).api

        coEvery { personRemoteDataSourceSpy.getPeopleApiClient() } returns remotePeopleApi
        every { downSyncScopeRepositorySpy.getDownSyncScope() } returns projectSyncScope
        every { secureLocalDbKeyProviderMock.getLocalDbKeyOrThrow(any()) } returns DEFAULT_LOCAL_DB_KEY
        every { loginInfoManagerMock.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
        every { loginInfoManagerMock.getSignedInUserIdOrEmpty() } returns DEFAULT_USER_ID
    }

    @Test
    fun syncByProjectSuccessfully() {
        runSyncTest { continuation, activity ->
            mockUploadPeople()
            val total = mockResponsesForSync(projectSyncScope)

            runAndVerifySyncSucceeds(activity, total + N_TO_UPLOAD, continuation)
        }
    }

    @Test
    fun syncByUserSuccessfully() {
        runSyncTest { continuation, activity ->
            val total = mockResponsesForSync(userSyncScope)
            runAndVerifySyncSucceeds(activity, total, continuation)
        }
    }


    @Test
    fun syncByModuleSuccessfully() {
        runSyncTest { continuation, activity ->
            val total = mockResponsesForSync(moduleSyncScope)
            runAndVerifySyncSucceeds(activity, total, continuation)
        }
    }

    @Test
    fun uploadFails_shouldSyncFail() {
        runSyncTest { continuation, activity ->
            mockResponsesForSync(projectSyncScope)
            mockDispatcher.uploadResponseResult = false

            runAndVerifySyncRetries(activity, continuation)
        }
    }

    @Test
    fun downloadFails_shouldSyncFail() {
        runSyncTest { continuation, activity ->
            mockResponsesForSync(projectSyncScope)
            mockDispatcher.downResponse = null

            runAndVerifySyncRetries(activity, continuation)
        }
    }

    @Test
    fun downCountFails_shouldSyncFail() {
        runSyncTest { continuation, activity ->
            mockResponsesForSync(projectSyncScope)
            mockDispatcher.countResponse = null

            runAndVerifySyncRetries(activity, continuation)
        }
    }


    private fun runAndVerifySyncSucceeds(act: Activity, total: Int, cor: CancellableContinuation<Boolean>) {
        peopleSyncManager.getLastSyncState().observe(act as LifecycleOwner, Observer {
            if (!it.anySyncWorkersStillRunning()) {
                it.assertSyncSucceeds(total)
                cor.resumeSafely(true)
            }
        })

        peopleSyncManager.sync()
    }

    private fun runAndVerifySyncRetries(act: Activity, cor: CancellableContinuation<Boolean>) {
        peopleSyncManager.getLastSyncState().observe(act as LifecycleOwner, Observer {
            if (!it.anySyncWorkersStillRunning()) {
                it.assertSyncRetries()
                cor.resumeSafely(true)
            }
        })

        peopleSyncManager.sync()
    }


    private fun runSyncTest(timeout: Long = 10000, block: (CancellableContinuation<Boolean>, act: Activity) -> Unit) =
        runBlocking {
            withTimeout(timeout) {
                suspendCancellableCoroutine<Boolean> { cor ->
                    runOnActivity<RequestLoginActivity> {
                        block(cor, it)
                    }
                }
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

        mockDispatcher.downResponse = apiPeopleToDownload
        mockDispatcher.countResponse = countResponse

        return apiPeopleToDownload.size
    }


    private fun mockUploadPeople() {
        val ops = runBlocking { downSyncScopeRepositorySpy.getDownSyncOperations(projectSyncScope) }
        coEvery { personLocalDataSourceSpy.load(any()) } returns getRandomPeople(N_TO_UPLOAD, ops.first(), listOf(true)).asFlow()
    }
}

class MockDispatcher : Dispatcher() {

    var countResponse: ApiPeopleOperationsResponse? = null
    var downResponse: List<ApiGetPerson>? = null
    var uploadResponseResult: Boolean = true

    override fun dispatch(request: RecordedRequest): MockResponse {
        val lastPart = request.requestUrl?.pathSegments?.last()

        return if (lastPart == "patients" && request.method == "POST") {
            MockResponse().setResponseCode(if (uploadResponseResult) 200 else 404)
        } else if (lastPart == "count") {
            MockResponse().setResponseCode(200).setBody(JsonHelper.gson.toJson(countResponse))
        } else if (lastPart == "patients" && request.method == "GET") {
            MockResponse().setResponseCode(200).setBody(JsonHelper.gson.toJson(downResponse))
        } else {
            MockResponse().setResponseCode(404)
        }
    }
}

private fun PeopleSyncState.anySyncWorkersStillRunning(): Boolean =
    downSyncStates.plus(upSyncStates).any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }

private fun PeopleSyncState.assertSyncRetries() {
    assertThat((downSyncStates.plus(upSyncStates)).any { it.state == WorkInfo.State.ENQUEUED }).isTrue()
}

private fun PeopleSyncState.assertSyncSucceeds(total: Int) {
    assertThat(total).isEqualTo(total)
    assertThat(progress).isEqualTo(total)
    assertThat((downSyncStates.plus(upSyncStates)).all { it.state == WorkInfo.State.SUCCEEDED }).isTrue()
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
