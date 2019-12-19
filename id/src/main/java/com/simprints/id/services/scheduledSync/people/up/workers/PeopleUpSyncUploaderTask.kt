package com.simprints.id.services.scheduledSync.people.up.workers

import com.google.firebase.FirebaseNetworkException
import com.simprints.id.data.db.people_sync.up.PeopleUpSyncScopeRepository
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperation
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperationResult
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperationResult.UpSyncState
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncOperationResult.UpSyncState.*
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.sync.TransientSyncFailureException
import com.simprints.id.services.scheduledSync.people.common.WorkerProgressCountReporter
import com.simprints.id.tools.extensions.bufferedChunks
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.IOException
import java.util.*

// TODO: uncomment userId when multitenancy is properly implemented

@InternalCoroutinesApi
class PeopleUpSyncUploaderTask(
    private val loginInfoManager: LoginInfoManager,
    private val personLocalDataSource: PersonLocalDataSource,
    private val personRemoteDataSource: PersonRemoteDataSource,
    private val batchSize: Int,
    private val peopleUpSyncScopeRepository: PeopleUpSyncScopeRepository
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

    suspend fun execute(workerProgressCountReporter: WorkerProgressCountReporter): Int {
        try {
            personLocalDataSource.load(PersonLocalDataSource.Query(toSync = true))
                .bufferedChunks(batchSize)
                .collect {
                    upSyncBatch(it)
                    count += it.size
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

    private fun uploadPeople(people: List<Person>) {
        try {
            if (people.isNotEmpty()) {
                personRemoteDataSource
                    .uploadPeople(projectId, people)
                    .blockingAwait()
            }
        } catch (exception: IOException) {
            throw TransientSyncFailureException(cause = exception)
        } catch (exception: SimprintsInternalServerException) {
            throw TransientSyncFailureException(cause = exception)
        } catch (exception: RuntimeException) {
            throw if (exception.cause is FirebaseNetworkException || exception.cause is IOException) {
                TransientSyncFailureException(cause = exception)
            } else {
                exception
            }
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
