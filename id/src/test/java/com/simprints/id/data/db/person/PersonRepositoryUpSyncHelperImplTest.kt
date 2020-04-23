package com.simprints.id.data.db.person

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.people_sync.up.PeopleUpSyncScopeRepository
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.domain.personevents.Events
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.EventRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.domain.modality.Modality
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Test
import java.util.*

class PersonRepositoryUpSyncHelperImplTest {
    private val loginInfoManager: LoginInfoManager = mockk(relaxed = true)
    private val personLocalDataSource: PersonLocalDataSource = mockk(relaxed = true)
    private val eventRemoteDataSource: EventRemoteDataSource = mockk(relaxed = true)
    private val peopleUpSyncScopeRepository: PeopleUpSyncScopeRepository = mockk(relaxed = true)
    private val peopleSyncCache: PeopleSyncCache = mockk(relaxed = true)
    private val modalities = listOf(Modality.FACE, Modality.FINGER)

    val personRepositoryUpSyncHelper = spyk(PersonRepositoryUpSyncHelperImpl(loginInfoManager,
            personLocalDataSource, eventRemoteDataSource, peopleUpSyncScopeRepository, modalities))

    private val uniqueWorkerId = "uniqueWorkerId"
    private val projectIdToSync = "projectIdToSync"
    private val userIdToSync = "userIdToSync"
    private val batchSize = 2

    private val notYetSyncedPerson1 = Person(
        "patientId1", "projectId", "userId", "moduleId", Date(1), null, true
    )
    private val notYetSyncedPerson2 = notYetSyncedPerson1.copy(patientId = "patientId2")
    private val notYetSyncedPerson3 = notYetSyncedPerson1.copy(patientId = "patientId3")
    private val events = personRepositoryUpSyncHelper.createEvents(listOf(notYetSyncedPerson1, notYetSyncedPerson2))

    private val syncedPerson1 = notYetSyncedPerson1.copy(toSync = false)
    private val syncedPerson2 = notYetSyncedPerson2.copy(toSync = false)
    private val syncedPerson3 = notYetSyncedPerson3.copy(toSync = false)

    @Before
    fun setUp() {
        UnitTestConfig(this)
            .coroutinesMainThread()
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

//    @Test
//    fun singleBatchNoConcurrentWrite() =
//        testSuccessfulUpSync(
//            localQueryResults = arrayOf(
//                listOf(notYetSyncedPerson1, notYetSyncedPerson2)
//            ),
//            expectedUploadBatches = personRepositoryUpSyncHelper.createEvents(
//                listOf(notYetSyncedPerson1, notYetSyncedPerson2)
//            ),
//            expectedLocalUpdates = arrayOf(
//                listOf(syncedPerson1, syncedPerson2)
//            )
//        )


    private fun testSuccessfulUpSync(
        localQueryResults: Array<List<Person>>,
        expectedUploadBatches: Events,
        expectedLocalUpdates: Array<List<Person>>
    ) {
        mockSignedInUser(projectIdToSync, userIdToSync)
        mockSuccessfulLocalPeopleQueries(*localQueryResults)
        mockSuccessfulLocalPeopleUpdates(*expectedLocalUpdates)
        every { personRepositoryUpSyncHelper.createEvents(any()) } returns events
        mockSyncStatusModel()

        runBlocking {
            personRepositoryUpSyncHelper.executeUploadWithProgress(this)
        }

        verifyLocalPeopleQueries(*localQueryResults)
        verifyPeopleUploads(expectedUploadBatches)
        verifyLocalPeopleUpdates(*expectedLocalUpdates)
    }

    private fun mockSignedInUser(projectId: String, userId: String) {
        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns projectId
        every { loginInfoManager.getSignedInUserIdOrEmpty() } returns userId
    }

    private fun mockSuccessfulLocalPeopleQueries(vararg queryResults: List<Person>) {
        coEvery { personLocalDataSource.load(any()) } coAnswers {
            queryResults.fold(emptyList<Person>()) { aggr, new -> aggr + new }.toList().asFlow()
        }
    }

    private fun mockSuccessfulLocalPeopleUpdates(vararg updates: List<Person>) {
        updates.forEach { update ->
            coEvery { personLocalDataSource.insertOrUpdate(any()) } returns Unit
        }
    }

    private fun mockSyncStatusModel() {
        coEvery { peopleUpSyncScopeRepository.insertOrUpdate(any()) } returns Unit
    }

    private fun verifyLocalPeopleQueries(vararg queryResults: List<Person>) {
        coVerify(exactly = 1) {
            personLocalDataSource.load(withArg {
                Truth.assertThat(it.toSync).isEqualTo(true)
            })
        }
    }

    private fun verifyPeopleUploads(events: Events) {
        coVerify(exactly = 1) { eventRemoteDataSource.post(projectIdToSync, events) }
    }

    private fun verifyLocalPeopleUpdates(vararg updates: List<Person>) {
        updates.forEach { update ->
            coVerify(exactly = 1) { personLocalDataSource.insertOrUpdate(update) }
        }
    }
}
