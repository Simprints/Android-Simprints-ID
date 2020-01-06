package com.simprints.id.services.scheduledSync.people.up.workers

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.people_sync.up.PeopleUpSyncScopeRepository
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.sync.TransientSyncFailureException
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncProgressCache
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import io.reactivex.Completable
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.util.*

class PeopleUpSyncUploaderTaskTest {

    private val loginInfoManager: LoginInfoManager = mockk(relaxed = true)
    private val personLocalDataSource: PersonLocalDataSource = mockk(relaxed = true)
    private val personRemoteDataSource: PersonRemoteDataSource = mockk(relaxed = true)
    private val peopleUpSyncScopeRepository: PeopleUpSyncScopeRepository = mockk(relaxed = true)
    private val peopleSyncProgressCache: PeopleSyncProgressCache = mockk(relaxed = true)

    private val uniqueWorkerId = "uniqueWorkerId"
    private val projectIdToSync = "projectIdToSync"
    private val userIdToSync = "userIdToSync"
    private val batchSize = 2

    private val task = PeopleUpSyncUploaderTask(
        loginInfoManager, personLocalDataSource, personRemoteDataSource ,
        batchSize, peopleUpSyncScopeRepository,
        peopleSyncProgressCache
    )

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
    }

    @Test
    fun userNotSignedIn1_shouldThrowIllegalStateException() {
        runBlocking {
            assertThrows<IllegalStateException> {
                task.execute(uniqueWorkerId, mockk(relaxed = true))
            }
        }
    }

    /* // TODO: uncomment userId when multitenancy is properly implemented
    @Test
    fun userNotSignedIn2_shouldThrowIllegalStateException() {
        mockSignedInUser(projectIdToSync, differentUserId)

        assertThrows<IllegalStateException> {
            task.execute()
        }
    }
    */

    @Test
    fun simprintsInternalServerException_shouldWrapInTransientSyncFailureException() {
        mockSignedInUser(projectIdToSync, userIdToSync)
        mockSuccessfulLocalPeopleQueries(listOf(notYetSyncedPerson1))
        every { personRemoteDataSource.uploadPeople(projectIdToSync, listOf(notYetSyncedPerson1)) } throws SimprintsInternalServerException()

        runBlocking {
            assertThrows<TransientSyncFailureException> {
                task.execute(uniqueWorkerId, mockk(relaxed = true))
            }
        }
    }

    @Test
    fun singleBatchNoConcurrentWrite() =
        testSuccessfulUpSync(
            localQueryResults = arrayOf(
                listOf(notYetSyncedPerson1, notYetSyncedPerson2)
            ),
            expectedUploadBatches = arrayOf(
                listOf(notYetSyncedPerson1, notYetSyncedPerson2)
            ),
            expectedLocalUpdates = arrayOf(
                listOf(syncedPerson1, syncedPerson2)
            )
        )

    @Test
    fun multipleBatchesNoConcurrentWrite() =
        testSuccessfulUpSync(
            localQueryResults = arrayOf(
                listOf(notYetSyncedPerson1, notYetSyncedPerson2, notYetSyncedPerson3)
            ),
            expectedUploadBatches = arrayOf(
                listOf(notYetSyncedPerson1, notYetSyncedPerson2),
                listOf(notYetSyncedPerson3)
            ),
            expectedLocalUpdates = arrayOf(
                listOf(syncedPerson1, syncedPerson2),
                listOf(syncedPerson3)
            )
        )

    @Test
    fun singleBatchConcurrentWrite() =
        testSuccessfulUpSync(
            localQueryResults = arrayOf(
                listOf(notYetSyncedPerson1, notYetSyncedPerson2),
                listOf(notYetSyncedPerson3)
            ),
            expectedUploadBatches = arrayOf(
                listOf(notYetSyncedPerson1, notYetSyncedPerson2),
                listOf(notYetSyncedPerson3)
            ),
            expectedLocalUpdates = arrayOf(
                listOf(syncedPerson1, syncedPerson2),
                listOf(syncedPerson3)
            )
        )

    private fun testSuccessfulUpSync(
        localQueryResults: Array<List<Person>>,
        expectedUploadBatches: Array<List<Person>>,
        expectedLocalUpdates: Array<List<Person>>
    ) {
        mockSignedInUser(projectIdToSync, userIdToSync)
        mockSuccessfulLocalPeopleQueries(*localQueryResults)
        mockSuccessfulPeopleUploads(*expectedUploadBatches)
        mockSuccessfulLocalPeopleUpdates(*expectedLocalUpdates)
        mockSyncStatusModel()

        runBlocking {
            task.execute(uniqueWorkerId, mockk(relaxed = true))
        }

        verifyLocalPeopleQueries(*localQueryResults)
        verifyPeopleUploads(*expectedUploadBatches)
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

    private fun mockSuccessfulPeopleUploads(vararg batches: List<Person>) {
        every { personRemoteDataSource.uploadPeople(any(), any()) } returns Completable.complete()
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
                assertThat(it.toSync).isEqualTo(true)
            })
        }
    }

    private fun verifyPeopleUploads(vararg batches: List<Person>) {
        batches.forEach { batch ->
            verify(exactly = 1) { personRemoteDataSource.uploadPeople(projectIdToSync, batch) }
        }
    }

    private fun verifyLocalPeopleUpdates(vararg updates: List<Person>) {
        updates.forEach { update ->
            coVerify(exactly = 1) { personLocalDataSource.insertOrUpdate(update) }
        }
    }
}
