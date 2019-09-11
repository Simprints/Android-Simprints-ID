package com.simprints.id.services.scheduledSync.peopleUpsync.uploader

import com.google.firebase.FirebaseNetworkException
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.syncstatus.upsyncinfo.UpSyncDao
import com.simprints.id.data.db.syncstatus.upsyncinfo.UpSyncStatus
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.sync.TransientSyncFailureException
import com.simprints.id.tools.extensions.bufferedChunks
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.IOException

// TODO: uncomment userId when multitenancy is properly implemented

@InternalCoroutinesApi
class PeopleUpSyncUploaderTask(
    private val loginInfoManager: LoginInfoManager,
    private val personLocalDataSource: PersonLocalDataSource,
    private val personRemoteDataSource: PersonRemoteDataSource,
    private val projectId: String,
    /*private val userId: String,*/
    private val batchSize: Int,
    private val upSyncStatusModel: UpSyncDao
) {

    /**
     * @throws TransientSyncFailureException if a temporary network / backend reason caused
     * the sync to fail.
     */

    suspend fun execute() {
        checkUserIsSignedIn()

        personLocalDataSource.load(PersonLocalDataSource.Query(toSync = true))
            .bufferedChunks(batchSize)
            .collect {
                upSyncBatch(it)
            }
    }

    private fun checkUserIsSignedIn() {
        if (projectId != loginInfoManager.signedInProjectId /*|| userId != loginInfoManager.signedInUserId*/) {
            throw IllegalStateException("Only people enrolled by the currently signed in user can be up-synced")
        }
    }

    private fun upSyncBatch(people: List<Person>) {
        uploadPeople(people)
        Timber.d("Uploaded a batch of ${people.size} people")
        markPeopleAsSynced(people)
        Timber.d("Marked a batch of ${people.size} people as synced")
        updateLastUpSyncTime()
    }

    private fun uploadPeople(people: List<Person>) =
        try {
            personRemoteDataSource
                .uploadPeople(projectId, people)
                .blockingAwait()
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

    private fun markPeopleAsSynced(people: List<Person>) {
        val updatedPeople = people.map { it.copy(toSync = false) }
        runBlocking { personLocalDataSource.insertOrUpdate(updatedPeople) }
    }

    private fun updateLastUpSyncTime() {
        upSyncStatusModel.insertLastUpSyncTime(UpSyncStatus(lastUpSyncTime = System.currentTimeMillis()))
    }

}
