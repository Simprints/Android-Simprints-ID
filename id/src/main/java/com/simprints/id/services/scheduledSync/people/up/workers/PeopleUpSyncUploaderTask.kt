package com.simprints.id.services.scheduledSync.people.up.workers

import com.simprints.id.data.db.people_sync.up.PeopleUpSyncScopeRepository
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperation
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperationResult
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperationResult.UpSyncState
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperationResult.UpSyncState.*
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.services.scheduledSync.people.common.WorkerProgressCountReporter
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.tools.extensions.bufferedChunks
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.*

// TODO: uncomment userId when multitenancy is properly implemented

@InternalCoroutinesApi
class PeopleUpSyncUploaderTask(
    private val loginInfoManager: LoginInfoManager,
    private val personLocalDataSource: PersonLocalDataSource,
    private val personRemoteDataSource: PersonRemoteDataSource,
    private val batchSize: Int,
    private val peopleUpSyncScopeRepository: PeopleUpSyncScopeRepository,
    private val cache: PeopleSyncCache
) {

    /**
     * @throws TransientSyncFailureException if a temporary network / backend reason caused
     * the sync to fail.
     */

    var count = 0

    val projectId: String
        get() {
            val projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
            return if (projectId.isEmpty() /*|| userId != loginInfoManager.signedInUserId*/) {
                throw IllegalStateException("Only people enrolled by the currently signed in user can be up-synced")
            } else {
                projectId
            }
        }

    suspend fun execute(workerId: String,
                        workerProgressCountReporter: WorkerProgressCountReporter): Int {
        try {
            count = cache.readProgress(workerId)
            workerProgressCountReporter.reportCount(count)

            personLocalDataSource.load(PersonLocalDataSource.Query(toSync = true))
                .bufferedChunks(batchSize)
                .collect {
                    upSyncBatch(it)
                    count += it.size

                    cache.saveProgress(workerId, count)
                    workerProgressCountReporter.reportCount(count)
                }

        } catch (t: Throwable) {
            t.printStackTrace()
            updateState(FAILED)
            throw t
        }

        updateState(COMPLETE)
        return count
    }

    private suspend fun upSyncBatch(people: List<Person>) {
        uploadPeople(people)
        Timber.d("Uploaded a batch of ${people.size} people")
        markPeopleAsSynced(people)
        Timber.d("Marked a batch of ${people.size} people as synced")
        updateState(RUNNING)
    }

    private suspend fun updateState(state: UpSyncState) {
        updateLastUpSyncTime(PeopleUpSyncOperation(
            projectId,
            PeopleUpSyncOperationResult(
                state,
                Date().time
            )
        ))
    }

    private suspend fun uploadPeople(people: List<Person>) {
        if (people.isNotEmpty()) {
            personRemoteDataSource.uploadPeople(projectId, people)
        }
    }

    private fun markPeopleAsSynced(people: List<Person>) {
        val updatedPeople = people.map { it.copy(toSync = false) }
        runBlocking { personLocalDataSource.insertOrUpdate(updatedPeople) }
    }

    private suspend fun updateLastUpSyncTime(peopleUpSyncOperation: PeopleUpSyncOperation) {
        peopleUpSyncScopeRepository.insertOrUpdate(peopleUpSyncOperation)
    }
}
