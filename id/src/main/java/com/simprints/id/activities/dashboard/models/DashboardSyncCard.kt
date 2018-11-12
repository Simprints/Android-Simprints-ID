package com.simprints.id.activities.dashboard.models

import com.simprints.id.activities.dashboard.views.DashboardSyncCardView
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.sync.models.SyncManagerState
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.tools.delegates.lazyVar
import io.reactivex.observers.DisposableObserver
import io.reactivex.rxkotlin.subscribeBy
import java.text.DateFormat
import javax.inject.Inject

class DashboardSyncCard(component: AppComponent,
                        type: DashboardCardType,
                        position: Int,
                        imageRes: Int,
                        title: String,
                        private val dateFormat: DateFormat) : DashboardCard(type, position, imageRes, title, "") {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var localDbManager: LocalDbManager

    var syncParams by lazyVar {
        SyncTaskParameters.build(preferencesManager.syncGroup, preferencesManager.moduleId, loginInfoManager)
    }

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

    init {
        component.inject(this)
        updateSyncInfo()
    }

    private fun updateSyncInfo() {
        updateLocalPeopleCount()
        updateLastSyncedTime()
    }

    private fun updateLastSyncedTime() {
        localDbManager
                .getSyncInfoFor(syncParams.toGroup())
                .subscribeBy(
                    onSuccess = {
                        lastSyncTime = dateFormat.format(it.lastSyncTime).toString()
                        cardView?.updateCard(this)
                    },
                    onError = { it.printStackTrace() })
    }

    private fun updateLocalPeopleCount() {
        localDbManager
                .getPeopleCountFromLocal(toSync = true)
            .subscribeBy(
                onSuccess = {
                    peopleToUpload = it
                    cardView?.updateCard(this)
                },
                onError = { it.printStackTrace() })
    }

    fun syncStarted() {
        syncState = SyncManagerState.STARTED
    }
}
