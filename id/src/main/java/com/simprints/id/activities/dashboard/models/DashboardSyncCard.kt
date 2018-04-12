package com.simprints.id.activities.dashboard.models

import com.simprints.id.activities.dashboard.views.DashboardSyncCardView
import com.simprints.id.data.DataManager
import com.simprints.id.data.db.sync.model.SyncManagerState
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.sync.SyncTaskParameters
import io.reactivex.observers.DisposableObserver
import java.text.DateFormat

class DashboardSyncCard(override val type: DashboardCardType,
                        override val position: Int,
                        override val imageRes: Int,
                        override val title: String,
                        var dataManager: DataManager,
                        private val dateFormat: DateFormat,
                        remotePeopleCount: Int) : DashboardCard(type, position, imageRes, title, "") {

    val syncParams = SyncTaskParameters.build(dataManager.syncGroup, dataManager)

    val peopleToUpload = dataManager.loadPeopleFromLocal(toSync = true).count()
    val peopleToDownload = dataManager.calculateNPatientsToDownSync(remotePeopleCount, syncParams)
    val syncNeeded = peopleToDownload > 0 || peopleToUpload > 0

    var onSyncActionClicked: (cardModel: DashboardSyncCard) -> Unit = {}

    val lastSyncTime: String? =
        dataManager.getSyncInfoFor(syncParams.toGroup())?.lastSyncTime?.let {
            dateFormat.format(it).toString()
        }

    var progress: Progress? = null

    var cardView: DashboardSyncCardView? = null

    var syncState: SyncManagerState = SyncManagerState.NOT_STARTED
        set(value) {
            field = value
            if(field != SyncManagerState.IN_PROGRESS) {
                this.progress = null
            }

            cardView?.updateState(this)
        }

    val syncObserver = object : DisposableObserver<Progress>() {

        override fun onNext(progressSync: Progress) {
            progress = progressSync
            syncState = SyncManagerState.IN_PROGRESS
        }

        override fun onComplete() {
            syncState = SyncManagerState.SUCCEED
        }

        override fun onError(throwable: Throwable) {
            syncState = SyncManagerState.FAILED
        }
    }

    fun syncStarted() {
        syncState = SyncManagerState.STARTED
    }
}

