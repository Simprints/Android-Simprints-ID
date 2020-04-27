package com.simprints.id.data.db.person

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.people_sync.up.PeopleUpSyncScopeRepository
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.domain.personevents.Events
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.EventRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.domain.modality.Modality
import com.simprints.id.exceptions.safe.sync.SyncCloudIntegrationException
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.channel.testChannel
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Test
import java.util.*

@ExperimentalCoroutinesApi
class PersonRepositoryUpSyncHelperImplTest {
    private val loginInfoManager: LoginInfoManager = mockk(relaxed = true)
    private val personLocalDataSource: PersonLocalDataSource = mockk(relaxed = true)
    private val eventRemoteDataSource: EventRemoteDataSource = mockk()
    private val peopleUpSyncScopeRepository: PeopleUpSyncScopeRepository = mockk(relaxed = true)
    private val modalities = listOf(Modality.FACE, Modality.FINGER)

    private val personRepositoryUpSyncHelper = spyk(PersonRepositoryUpSyncHelperImpl(loginInfoManager,
            personLocalDataSource, eventRemoteDataSource, peopleUpSyncScopeRepository, modalities))

    private val projectIdToSync = "projectIdToSync"
    private val userIdToSync = "userIdToSync"
    private val batchSize = 2

    private val notYetSyncedPerson1 = Person(
        "patientId1", "projectId", "userId", "moduleId", Date(1), null, true
    )
    private val notYetSyncedPerson2 = notYetSyncedPerson1.copy(patientId = "patientId2")
    private val notYetSyncedPerson3 = notYetSyncedPerson1.copy(patientId = "patientId3")

    private val syncedPerson1 = notYetSyncedPerson1.copy(toSync = false)
    private val syncedPerson2 = notYetSyncedPerson2.copy(toSync = false)
    private val syncedPerson3 = notYetSyncedPerson3.copy(toSync = false)

    @Before
    fun setUp() {
        UnitTestConfig(this)
            .coroutinesMainThread()
        setupBatchSize()
        mockHelperToGenerateSameUuidForEvents()
    }

    @Test
    fun userNotSignedIn1_shouldThrowIllegalStateException() {
        runBlocking {
            val expectedExceptionMessage = "People can only be uploaded when signed in"
            
            val exceptionMessage = assertThrows<IllegalStateException> {
                withContext(Dispatchers.IO) {
                    personRepositoryUpSyncHelper.executeUploadWithProgress(this)
                }
            }.message
            assertThat(exceptionMessage).isEqualTo(expectedExceptionMessage)
        }
    }

    @Test
    fun simprintsInternalServerException_shouldWrapInTransientSyncFailureException() {
        runBlocking {
            mockSignedInUser(projectIdToSync, userIdToSync)
            mockSuccessfulLocalPeopleQueries(listOf(notYetSyncedPerson1))
            coEvery { eventRemoteDataSource.post(projectIdToSync, any()) } throws SyncCloudIntegrationException("", Throwable())

            assertThrows<SyncCloudIntegrationException> {
                withContext(Dispatchers.IO) {
                    personRepositoryUpSyncHelper.executeUploadWithProgress(this)
                }
            }
        }
    }

    @Test
    fun singleBatchNoConcurrentWrite() {
        val peopleBatches = arrayOf(listOf(notYetSyncedPerson1, notYetSyncedPerson2))
        val events = createEventsFromPeople(
            arrayOf(
                listOf(notYetSyncedPerson1, notYetSyncedPerson2)
            )
        )

        testSuccessfulUpSync(
            localQueryResults = peopleBatches,
            expectedUploadBatches = events,
            expectedLocalUpdates = arrayOf(listOf(syncedPerson1, syncedPerson2))
        )
    }

    @Test
    fun multipleBatchesNoConcurrentWrite() {
        val peopleBatches = arrayOf(listOf(notYetSyncedPerson1, notYetSyncedPerson2, notYetSyncedPerson3))
        val events = createEventsFromPeople(
            arrayOf(
                listOf(notYetSyncedPerson1, notYetSyncedPerson2),
                listOf(notYetSyncedPerson3)
            )
        )

        testSuccessfulUpSync(
            localQueryResults = peopleBatches,
            expectedUploadBatches = events,
            expectedLocalUpdates = arrayOf(
                listOf(syncedPerson1, syncedPerson2),
                listOf(syncedPerson3)
            )
        )
    }

    @Test
    fun singleBatchConcurrentWrite() {
        val peopleBatches = arrayOf(
            listOf(notYetSyncedPerson1, notYetSyncedPerson2),
            listOf(notYetSyncedPerson3)
        )
        val events = createEventsFromPeople(
            arrayOf(
                listOf(notYetSyncedPerson1, notYetSyncedPerson2),
                listOf(notYetSyncedPerson3)
            )
        )

        testSuccessfulUpSync(
            localQueryResults = peopleBatches,
            expectedUploadBatches = events,
            expectedLocalUpdates = arrayOf(
                listOf(syncedPerson1, syncedPerson2),
                listOf(syncedPerson3)
            )
        )
    }

    private fun setupBatchSize() {
        every { personRepositoryUpSyncHelper.batchSize } returns batchSize
    }

    private fun mockHelperToGenerateSameUuidForEvents() {
        every { personRepositoryUpSyncHelper.getRandomUuid() } returns "random_uuid"
    }

    private fun testSuccessfulUpSync(
        localQueryResults: Array<List<Person>>,
        expectedUploadBatches: Array<Events>,
        expectedLocalUpdates: Array<List<Person>>
    ) {
        runBlocking {

            mockSignedInUser(projectIdToSync, userIdToSync)
            mockSuccessfulLocalPeopleQueries(*localQueryResults)
            mockSuccessfulLocalPeopleUpdates()
            mockEventRemoteDataSource()
            mockSyncStatusModel()

            withContext(Dispatchers.IO) {
                personRepositoryUpSyncHelper.executeUploadWithProgress(this).testChannel()
                verifyLocalPeopleQueries()
                verifyPeopleUploads(expectedUploadBatches)
                verifyLocalPeopleUpdates(*expectedLocalUpdates)
            }
        }

    }

    private fun createEventsFromPeople(people: Array<List<Person>>) =
        people.map {
            personRepositoryUpSyncHelper.createEvents(it)
        }.toTypedArray()

    private fun mockSignedInUser(projectId: String, userId: String) {
        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns projectId
        every { loginInfoManager.getSignedInUserIdOrEmpty() } returns userId
    }

    private fun mockSuccessfulLocalPeopleQueries(vararg queryResults: List<Person>) {
        coEvery { personLocalDataSource.load(any()) } coAnswers {
            queryResults.fold(emptyList<Person>()) { aggr, new -> aggr + new }.toList().asFlow()
        }
    }

    private fun mockSuccessfulLocalPeopleUpdates() {
        coEvery { personLocalDataSource.insertOrUpdate(any()) } returns Unit
    }

    private fun mockEventRemoteDataSource() {
        coEvery{ eventRemoteDataSource.post(projectIdToSync, any()) } returns Unit
    }

    private fun mockSyncStatusModel() {
        coEvery { peopleUpSyncScopeRepository.insertOrUpdate(any()) } returns Unit
    }

    private fun verifyLocalPeopleQueries() {
        coVerify(exactly = 1) {
            personLocalDataSource.load(withArg {
                assertThat(it.toSync).isEqualTo(true)
            })
        }
    }

    private fun verifyPeopleUploads(events: Array<Events>) {
        events.forEach {
            coVerify(exactly = 1) { eventRemoteDataSource.post(projectIdToSync, it) }
        }
    }

    private fun verifyLocalPeopleUpdates(vararg updates: List<Person>) {
        updates.forEach { update ->
            coVerify(exactly = 1) { personLocalDataSource.insertOrUpdate(update) }
        }
    }
}
