package com.simprints.id.data.db.person

import com.simprints.id.commontesttools.DefaultTestConstants
import com.simprints.id.commontesttools.EnrolmentRecordsGeneratorUtils.getRandomEnrolmentEvents
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationFactoryImpl
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.EventRemoteDataSource
import com.simprints.id.data.db.person.remote.models.personevents.ApiEvent
import com.simprints.id.data.db.person.remote.models.personevents.fromDomainToApi
import com.simprints.id.domain.modality.Modes
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.id.tools.json.SimJsonHelper
import com.simprints.testtools.common.channel.testChannel
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test

class PersonRepositoryDownSyncHelperImplTest {

    val builder = PeopleDownSyncOperationFactoryImpl()
    private val modes = listOf(Modes.FACE, Modes.FINGERPRINT)
    private val projectSyncOp = PeopleDownSyncOperation(
        DefaultTestConstants.DEFAULT_PROJECT_ID,
        null,
        null,
        listOf(Modes.FINGERPRINT),
        null
    )

    private val userSyncOp = PeopleDownSyncOperation(
        DefaultTestConstants.DEFAULT_PROJECT_ID,
        DefaultTestConstants.DEFAULT_USER_ID,
        null,
        listOf(Modes.FINGERPRINT),
        null
    )

    private val moduleSyncOp = PeopleDownSyncOperation(
        DefaultTestConstants.DEFAULT_PROJECT_ID,
        null,
        DefaultTestConstants.DEFAULT_MODULE_ID,
        listOf(Modes.FINGERPRINT),
        null
    )

    @RelaxedMockK lateinit var personLocalDataSource: PersonLocalDataSource
    @RelaxedMockK lateinit var eventRemoteDataSource: EventRemoteDataSource
    @RelaxedMockK lateinit var peopleDownSyncScopeRepository: PeopleDownSyncScopeRepository
    val timeHelper = TimeHelperImpl()

    @Before
    fun setUp() {
        UnitTestConfig(this)
            .coroutinesMainThread()
        MockKAnnotations.init(this)
    }


    @Test
    fun downloadPatientsForGlobalSync_shouldSuccess() {
        runBlocking {
            val nEventsToDownload = 407
            val nEventsToDelete = 0

            runDownSyncAndVerifyConditions(nEventsToDownload, nEventsToDelete, projectSyncOp)
        }
    }

    @ExperimentalCoroutinesApi
    private fun runDownSyncAndVerifyConditions(nEventsToDownload: Int,
                                               nEventsToDelete: Int,
                                               syncOp: PeopleDownSyncOperation) {

        runBlocking {
            mockLocalDataSource()
            mockEventRemoteDataSource(nEventsToDownload)
            mockDownSyncScopeRepository()

            val downSyncHelper =
                PersonRepositoryDownSyncHelperImpl(personLocalDataSource, eventRemoteDataSource,
                    peopleDownSyncScopeRepository, timeHelper)

            withContext(Dispatchers.IO) {
                downSyncHelper.performDownSyncWithProgress(this, syncOp, mockk()).testChannel()
            }
        }
    }

    private fun mockLocalDataSource() {
        coEvery { personLocalDataSource.insertOrUpdate(any()) } returns Unit
        coEvery { personLocalDataSource.delete(any()) } returns Unit
    }

    private fun mockEventRemoteDataSource(nEvents: Int) {
        val events = getRandomEnrolmentEvents(nEvents, DefaultTestConstants.DEFAULT_PROJECT_ID,
            DefaultTestConstants.DEFAULT_USER_ID, DefaultTestConstants.DEFAULT_MODULE_ID).map { it.fromDomainToApi() }
        coEvery { eventRemoteDataSource.getStreaming(any()) } returns buildResponse(events)
    }

    private fun buildResponse(events: List<ApiEvent>): ResponseBody =
        SimJsonHelper.gson.toJson(events).toString().toResponseBody(null)

    private fun mockDownSyncScopeRepository() {
        coEvery { peopleDownSyncScopeRepository.insertOrUpdate(any())  } returns Unit
    }
}
