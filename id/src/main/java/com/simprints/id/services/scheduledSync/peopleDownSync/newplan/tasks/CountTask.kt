package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.tasks

import io.reactivex.Completable

/**
 * Ridwan - CountTask: task to:
 * a) Make NetworkRequest
 * b) InsertOrUpdate DownSyncStatus(p,u,m).totalToDownload = X in Room
 */
class CountTask(val projectId: String, val userId: String?, val moduleId: String?) {
    fun execute(): Completable
}
