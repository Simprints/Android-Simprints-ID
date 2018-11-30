package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.tasks

import io.reactivex.Completable

/**
 * Ridwan - DownSyncTask: task to:
 * a) Make NetworkRequest
 * b) save patients in Realm
 * c) InsertOrUpdate
 *      DownSyncStatus(p,u,m).LastPatientId = X
 *      DownSyncStatus(p,u,m).LastPatientUpdatedAt = X
 *      DownSyncStatus(p,u,m).LastSyncTime = X
 */
class DownSyncTask(val projectId: String, val userId: String?, val moduleId: String?) {
    fun execute(): Completable = Completable.complete()
}
