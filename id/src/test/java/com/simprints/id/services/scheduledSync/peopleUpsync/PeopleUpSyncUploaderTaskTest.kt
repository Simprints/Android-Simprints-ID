package com.simprints.id.services.scheduledSync.peopleUpsync

import com.nhaarman.mockito_kotlin.*
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.domain.Person
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.sync.TransientSyncFailureException
import com.simprints.id.data.db.local.room.UpSyncDao
import com.simprints.id.services.scheduledSync.peopleUpsync.uploader.PeopleUpSyncUploaderTask
import com.simprints.id.shared.assertThrows
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.junit.Test
import java.text.DateFormat
import java.util.*

class PeopleUpSyncUploaderTaskTest {

    private val loginInfoManager: LoginInfoManager = mock()
    private val localDbManager: LocalDbManager = mock()
    private val remoteDbManager: RemoteDbManager = mock()
    private val upSyncDao: UpSyncDao = mock()

    private val projectIdToSync = "projectIdToSync"
    private val userIdToSync = "userIdToSync"
    private val batchSize = 2

    private val task = PeopleUpSyncUploaderTask(
        loginInfoManager, localDbManager, remoteDbManager,
        projectIdToSync, /*userIdToSync, */batchSize, upSyncDao // TODO: uncomment userId when multitenancy is properly implemented
    )

    private val differentProjectId = "differentProjectId"
    private val differentUserId = "differentUserId"

    private val notYetSyncedPerson1 = Person(
        "patientId1", "projectId", "userId", "moduleId",
        Date(1), null, true, emptyList()
    )
    private val notYetSyncedPerson2 = notYetSyncedPerson1.copy(patientId = "patientId2")
    private val notYetSyncedPerson3 = notYetSyncedPerson1.copy(patientId = "patientId3")

    private val syncedPerson1 = notYetSyncedPerson1.copy(toSync = false)
    private val syncedPerson2 = notYetSyncedPerson2.copy(toSync = false)
    private val syncedPerson3 = notYetSyncedPerson3.copy(toSync = false)

    private val dateFormat: DateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault())
    }

    @Test
    fun userNotSignedIn1_shouldThrowIllegalStateException() {
        mockSignedInUser(differentProjectId, userIdToSync)

        assertThrows<IllegalStateException> {
            task.execute()
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
        whenever(remoteDbManager.uploadPeople(projectIdToSync, listOf(notYetSyncedPerson1)))
            .thenThrow(SimprintsInternalServerException())

        assertThrows<TransientSyncFailureException> {
            task.execute()
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

        task.execute()

        verifyLocalPeopleQueries(*localQueryResults)
        verifyPeopleUploads(*expectedUploadBatches)
        verifyLocalPeopleUpdates(*expectedLocalUpdates)
    }

    private fun mockSignedInUser(projectId: String, userId: String) {
        whenever(loginInfoManager.signedInProjectId).thenReturn(projectId)
        whenever(loginInfoManager.signedInUserId).thenReturn(userId)
    }

    private fun mockSuccessfulLocalPeopleQueries(vararg queryResults: List<Person>) {
        queryResults
            .fold(whenever(localDbManager.getPeopleCountFromLocal(/*userId = userIdToSync, */toSync = true))) { ongoingStub, queryResult ->
                ongoingStub.thenReturn(Single.just(queryResult.size))
            }
            .thenReturn(Single.just(0))
        queryResults
            .fold(whenever(localDbManager.loadPeopleFromLocalRx(/*userId = userIdToSync, */toSync = true))) { ongoingStub, queryResult ->
                ongoingStub.thenReturn(Flowable.fromIterable(queryResult))
            }
    }

    private fun mockSuccessfulPeopleUploads(vararg batches: List<Person>) {
        batches.forEach { batch ->
            whenever(remoteDbManager.uploadPeople(projectIdToSync, batch)).thenReturn(Completable.complete())
        }
    }

    private fun mockSuccessfulLocalPeopleUpdates(vararg updates: List<Person>) {
        updates.forEach { update ->
            whenever(localDbManager.insertOrUpdatePeopleInLocal(update)).thenReturn(Completable.complete())
        }
    }

    private fun mockSyncStatusModel() {
        whenever(upSyncDao.insertLastUpSyncTime(any())).then { }
    }

    private fun verifyLocalPeopleQueries(vararg queryResults: List<Person>) {
        verify(localDbManager, times(queryResults.size + 1)).getPeopleCountFromLocal(/*userId = userIdToSync, */toSync = true)
        verify(localDbManager, times(queryResults.size)).loadPeopleFromLocalRx(/*userId = userIdToSync, */toSync = true)
    }

    private fun verifyPeopleUploads(vararg batches: List<Person>) {
        batches.forEach { batch ->
            verify(remoteDbManager).uploadPeople(projectIdToSync, batch)
        }
    }

    private fun verifyLocalPeopleUpdates(vararg updates: List<Person>) {
        updates.forEach { update ->
            verify(localDbManager).insertOrUpdatePeopleInLocal(update)
        }
    }
}
