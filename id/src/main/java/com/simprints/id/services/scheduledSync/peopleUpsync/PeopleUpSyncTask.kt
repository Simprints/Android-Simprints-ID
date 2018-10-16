package com.simprints.id.services.scheduledSync.peopleUpsync

import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.domain.Person
import io.reactivex.Flowable
import timber.log.Timber

class PeopleUpSyncTask(
    private val loginInfoManager: LoginInfoManager,
    private val localDbManager: LocalDbManager,
    private val remoteDbManager: RemoteDbManager,
    private val projectId: String,
    private val userId: String,
    private val batchSize: Int
) {

    fun execute() {
        checkUserIsSignedIn()

        while (thereArePeopleToSync()) {
            getPeopleToSyncInBatches()
                .blockingForEach(::upSyncBatch)
        }
    }

    private fun checkUserIsSignedIn() {
        if (projectId != loginInfoManager.signedInProjectId || userId != loginInfoManager.signedInUserId) {
            throw IllegalStateException("Only people enrolled by the currently signed in user can be up-synced")
        }
    }

    private fun thereArePeopleToSync(): Boolean {
        val peopleToSyncCount = localDbManager
            .getPeopleCountFromLocal(userId = userId, toSync = true)
            .blockingGet()
        Timber.d("$peopleToSyncCount people to up-sync")
        return peopleToSyncCount > 0
    }

    private fun getPeopleToSyncInBatches(): Flowable<List<Person>> =
        localDbManager.loadPeopleFromLocalRx(userId = userId, toSync = true)
            .buffer(batchSize)

    private fun upSyncBatch(people: List<Person>) {
        uploadPeople(people)
        Timber.d("Uploaded a batch of ${people.size} people")
        markPeopleAsSynced(people)
        Timber.d("Marked a batch of ${people.size} people as synced")
    }

    private fun uploadPeople(people: List<Person>) =
        remoteDbManager
            .uploadPeople(projectId, people)
            .blockingAwait()

    private fun markPeopleAsSynced(people: List<Person>) {
        val updatedPeople = people.map { it.copy(toSync = false) }
        localDbManager
            .insertOrUpdatePeopleInLocal(updatedPeople)
            .blockingAwait()
    }

}
