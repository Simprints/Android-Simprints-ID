package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.tasks

import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.room.DownSyncDao
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Ridwan - CountTask: task to:
 * a) Make NetworkRequest - DONE
 * b) InsertOrUpdate DownSyncStatus(p,u,m).totalToDownload = X in Room
 */
class CountTask(component: AppComponent,
                private val downSyncStatusId: String,
                private val projectId: String,
                private val userId: String?,
                private val moduleId: String?) {

    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var downSyncDao: DownSyncDao // STOPSHIP : inject or constructor?

    init {
        component.inject(this)
    }

    fun execute(): Completable =
        remoteDbManager
            .getNumberOfPatients(projectId, userId, moduleId)
            .calculateNPatientsToDownSync(projectId, userId, moduleId)
            .insertNewCountForDownSyncStatus(downSyncStatusId)

    private fun Single<out Int>.calculateNPatientsToDownSync(projectId: String, userId: String?, moduleId: String?) =
        flatMap {
            dbManager.calculateNPatientsToDownSync(it, projectId, userId, moduleId)
        }

    private fun Single<out Int>.insertNewCountForDownSyncStatus(downSyncStatusId: String) =
        flatMapCompletable {
            downSyncDao.updatePeopleToDownSync(downSyncStatusId, it)
            Completable.complete()
        }
}
