package com.simprints.id.services.scheduledSync.peopleUpsync.uploader

import com.google.firebase.FirebaseNetworkException
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.domain.Person
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.sync.TransientSyncFailureException
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.room.UpSyncDao
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.room.UpSyncStatus
import io.reactivex.Flowable
import timber.log.Timber
import java.io.IOException

// TODO: uncomment userId when multitenancy is properly implemented

class PeopleUpSyncUploaderTask (
    private val loginInfoManager: LoginInfoManager,
    private val localDbManager: LocalDbManager,
    private val remoteDbManager: RemoteDbManager,
    private val projectId: String,
    /*private val userId: String,*/
    private val batchSize: Int,
    private val upSyncStatusModel: UpSyncDao
) {

    /**
     * @throws TransientSyncFailureException if a temporary network / backend error caused
     * the sync to fail.
     */

    fun execute() {
        checkUserIsSignedIn()

        while (thereArePeopleToSync()) {
            getPeopleToSyncInBatches()
                .blockingForEach(::upSyncBatch)
        }
    }

    private fun checkUserIsSignedIn() {
        if (projectId != loginInfoManager.signedInProjectId /*|| userId != loginInfoManager.signedInUserId*/) {
            throw IllegalStateException("Only people enrolled by the currently signed in user can be up-synced")
        }
    }

    private fun thereArePeopleToSync(): Boolean {
        val peopleToSyncCount = localDbManager
            .getPeopleCountFromLocal(/*userId = userId, */toSync = true)
            .blockingGet()
        Timber.d("$peopleToSyncCount people to up-sync")
        return peopleToSyncCount > 0
    }

    private fun getPeopleToSyncInBatches(): Flowable<List<Person>> =
        localDbManager.loadPeopleFromLocalRx(/*userId = userId, */toSync = true)
            .buffer(batchSize)

    private fun upSyncBatch(people: List<Person>) {
        uploadPeople(people)
        Timber.d("Uploaded a batch of ${people.size} people")
        markPeopleAsSynced(people)
        Timber.d("Marked a batch of ${people.size} people as synced")
        updateLastUpSyncTime()
    }

    private fun uploadPeople(people: List<Person>) =
        try {
            remoteDbManager
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
        localDbManager
            .insertOrUpdatePeopleInLocal(updatedPeople)
            .blockingAwait()
    }

    private fun updateLastUpSyncTime() {
        upSyncStatusModel.insertLastUpSyncTime(UpSyncStatus(lastUpSyncTime = System.currentTimeMillis()))
    }

}
