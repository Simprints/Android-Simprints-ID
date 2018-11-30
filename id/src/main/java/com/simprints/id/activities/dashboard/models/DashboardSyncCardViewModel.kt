package com.simprints.id.activities.dashboard.models

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.work.State
import androidx.work.WorkManager
import androidx.work.WorkStatus
import com.simprints.id.activities.dashboard.views.DashboardSyncCardView
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.room.*
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.DownSyncWorker
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.tools.delegates.lazyVar
import io.reactivex.rxkotlin.subscribeBy
import java.text.DateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class DashboardSyncCardViewModel(private val lifecycleOwner: LifecycleOwner,
                                 component: AppComponent,
                                 type: DashboardCardType,
                                 position: Int,
                                 imageRes: Int,
                                 title: String) : DashboardCard(type, position, imageRes, title, "") {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var localDbManager: LocalDbManager
    @Inject lateinit var newSyncStatusDatabase: NewSyncStatusDatabase

    var syncParams by lazyVar {
        SyncTaskParameters.build(preferencesManager.syncGroup,
            preferencesManager.selectedModules, loginInfoManager)
    }

    private val dateFormat: DateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault())
    }

    var cardView: DashboardSyncCardView? = null
    private val newSyncStatusViewModel: NewSyncStatusViewModel

    private var latestDownSyncTime: Long? = null
    private var latestUpSyncTime: Long? = null
    private var syncState: State? = null

    var onSyncActionClicked: (cardModel: DashboardSyncCardViewModel) -> Unit = {}
    var peopleToUpload = 0
    var peopleToDownload = 0
    var peopleInDb = 0
    var isSyncRunning = false
        get() = syncState == State.RUNNING

    var lastSyncTime = ""

    init {
        component.inject(this)

        val syncScope = SyncScope(syncParams.projectId, syncParams.userId, syncParams.moduleIds)

        newSyncStatusViewModel = NewSyncStatusViewModel(
            newSyncStatusDatabase.downSyncStatusModel,
            newSyncStatusDatabase.upSyncStatusModel,
            syncScope)

        observeAndUpdatePeopleToDownloadAndLatestDownSyncTimes()
        observeAndUpdateLatestUpSyncTime()
        observeDownSyncStatusAndUpdateButton()

        updateSyncInfo()
    }

    private fun updateSyncInfo() {
        updateTotalLocalPeopleCount()
        updateLocalPeopleToUpSyncCount()
    }

    private fun updateTotalLocalPeopleCount() {
        dbManager.getPeopleCountFromLocalForSyncGroup(preferencesManager.syncGroup).subscribeBy(
            onSuccess = {
                peopleInDb = it
                updateCardView()
            },
            onError = { it.printStackTrace() })
    }

    private fun updateLocalPeopleToUpSyncCount() {
        localDbManager
            .getPeopleCountFromLocal(toSync = true)
            .subscribeBy(
                onSuccess = {
                    peopleToUpload = it
                    updateCardView()
                },
                onError = { it.printStackTrace() })
    }

    private fun observeAndUpdatePeopleToDownloadAndLatestDownSyncTimes() {

        val downSyncStatusObserver = Observer<List<DownSyncStatus>> { downSyncStatuses ->
            var peopleToDownSync = 0
            val latestDownSyncTimeForModules: ArrayList<Long> = ArrayList()

            downSyncStatuses.forEach {
                peopleToDownSync += it.totalToDownload
                it.lastSyncTime?.let { lastSyncTime -> latestDownSyncTimeForModules.add(lastSyncTime) }
            }
            peopleToDownload = peopleToDownSync
            latestDownSyncTime = latestDownSyncTimeForModules.max()

            lastSyncTime = calculateLatestSyncTimeIfPossible(latestDownSyncTime, latestUpSyncTime)
            updateCardView()
        }
       newSyncStatusViewModel.downSyncStatus.observe(lifecycleOwner, downSyncStatusObserver)
    }

    private fun observeAndUpdateLatestUpSyncTime() {

        val upSyncObserver = Observer<UpSyncStatus?> {
            latestUpSyncTime = it?.lastUpSyncTime
            lastSyncTime = calculateLatestSyncTimeIfPossible(latestDownSyncTime, latestUpSyncTime)
            updateCardView()
        }
        newSyncStatusViewModel.upSyncStatus.observe(lifecycleOwner, upSyncObserver)
    }

    private fun calculateLatestSyncTimeIfPossible(lastDownSyncTime: Long?, lastUpSyncTime: Long?): String {
        val lastDownSyncDate = lastDownSyncTime?.let { Date(it) }
        val lastUpSyncDate = lastUpSyncTime?.let { Date(it) }

        if (lastDownSyncDate != null && lastUpSyncDate != null) {
            return if (lastDownSyncDate.after(lastUpSyncDate)) {
                lastDownSyncDate.toString()
            } else {
                lastUpSyncDate.toString()
            }
        }

        lastDownSyncDate?.let { return dateFormat.format(it) }
        lastUpSyncDate?.let { return dateFormat.format(it) }

        return ""
    }


    private fun observeDownSyncStatusAndUpdateButton() {

        val downSyncObserver = Observer<List<WorkStatus>> { it ->
            val possibleSyncState = it.find { it.state == State.RUNNING }

            if (possibleSyncState != null) {
//                val currentSyncState = it.last().state
//                if(syncState != currentSyncState) {
//                    syncState = currentSyncState
                    updateSyncInfo()
                    updateCardView()
//                }
            }
        }

        //TODO: Observe DownSyncWorker/s here
        newSyncStatusViewModel.downSyncWorkerStatus.observe(lifecycleOwner, downSyncObserver)
    }

    private fun updateCardView() = cardView?.bind(this)

    class NewSyncStatusViewModel(
        downSyncDbModel: DownSyncDao,
        upSyncDbModel: UpSyncDao,
        syncScope: SyncScope) {

        val downSyncStatus = downSyncDbModel.getDownSyncStatus()

        val upSyncStatus = upSyncDbModel.getUpSyncStatus()

        val downSyncWorkerStatus = WorkManager.getInstance().getStatusesByTag("${DownSyncWorker.DOWNSYNC_WORKER_TAG}_$syncScope")
    }
}
