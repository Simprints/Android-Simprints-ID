package com.simprints.id.activities.dashboard.models

import com.simprints.id.activities.dashboard.views.DashboardSyncCardView
import com.simprints.id.data.DataManager
import com.simprints.id.data.db.sync.model.SyncManagerState
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.sync.SyncTaskParameters
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.rxkotlin.subscribeBy
import java.text.DateFormat

class DashboardSyncCard(type: DashboardCardType,
                        position: Int,
                        imageRes: Int,
                        title: String,
                        var dataManager: DataManager,
                        private val dateFormat: DateFormat) : DashboardCard(type, position, imageRes, title, "") {

    val syncParams = SyncTaskParameters.build(dataManager.syncGroup, dataManager)
    var onSyncActionClicked: (cardModel: DashboardSyncCard) -> Unit = {}
    var peopleToUpload: Int = 0
    var peopleToDownload: Int? = null
    var syncNeeded: Boolean = false
    var lastSyncTime: String? = null
    var progress: Progress? = null
    var cardView: DashboardSyncCardView? = null

    var syncState: SyncManagerState = SyncManagerState.NOT_STARTED
        set(value) {
            field = value
            if (field != SyncManagerState.IN_PROGRESS) {
                this.progress = null
            }

            when (field) {
                SyncManagerState.NOT_STARTED -> cardView?.updateState(this)
                SyncManagerState.IN_PROGRESS -> cardView?.updateState(this)
                SyncManagerState.SUCCEED -> updateSyncInfo()
                SyncManagerState.FAILED -> updateSyncInfo()
                SyncManagerState.STARTED -> updateSyncInfo()
            }
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

    init {
        updateSyncInfo()
    }

    private fun updateSyncInfo() {
        peopleToUpload = dataManager.loadPeopleFromLocal(toSync = true).count()
        lastSyncTime = dataManager.getSyncInfoFor(syncParams.toGroup())?.lastSyncTime?.let {
            dateFormat.format(it).toString()
        }

        dataManager.getNumberOfPatientsForSyncParams(syncParams)
            .map {
                dataManager.calculateNPatientsToDownSync(it, syncParams)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy (
                onSuccess = {
                    peopleToDownload = it
                    syncNeeded = it > 0 || peopleToUpload > 0
                    cardView?.updateCard(this)
                },
                onError = {
                    it.printStackTrace()
                    peopleToDownload = null
                    syncNeeded = true
                    cardView?.updateCard(this)
                }
            )
    }

    fun syncStarted() {
        syncState = SyncManagerState.STARTED
    }
}
