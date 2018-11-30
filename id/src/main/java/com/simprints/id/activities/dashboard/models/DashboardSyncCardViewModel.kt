package com.simprints.id.activities.dashboard.models

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.work.State
import androidx.work.WorkManager
import androidx.work.WorkStatus
import com.simprints.id.activities.dashboard.views.DashboardSyncCardView
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.sync.room.SyncStatusDao
import com.simprints.id.data.db.sync.room.SyncStatusDatabase
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.peopleDownSync.PeopleDownSyncMaster
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.room.*
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
    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase
    @Inject lateinit var newSyncStatusDatabase: NewSyncStatusDatabase

    var syncParams by lazyVar {
        SyncTaskParameters.build(preferencesManager.syncGroup,
            preferencesManager.selectedModules, loginInfoManager)
    }

    private val dateFormat: DateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault())
    }

    var cardView: DashboardSyncCardView? = null
    private val syncStatusViewModel: SyncStatusViewModel
    private val newSyncStatusViewModel: NewSyncStatusViewModel

    var latestDownSyncTime: Long? = null
    var latestUpSyncTime: Long? = null

    var onSyncActionClicked: (cardModel: DashboardSyncCardViewModel) -> Unit = {}
    var peopleToUpload = 0
    var peopleToDownload = 0
    var peopleInDb = 0
    var syncState: State? = null
    var isSyncRunning = false
        get() = syncState == State.RUNNING

    var lastSyncTime = ""

    init {
        component.inject(this)
        syncStatusViewModel = SyncStatusViewModel(
            syncStatusDatabase.syncStatusModel,
            preferencesManager.projectId)

        newSyncStatusViewModel = NewSyncStatusViewModel(
            newSyncStatusDatabase.downSyncStatusModel,
            newSyncStatusDatabase.upSyncStatusModel)

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
            calculateLatestSyncTimeIfPossible(latestDownSyncTime, latestUpSyncTime)
        }
        newSyncStatusViewModel.downSyncStatus.observe(lifecycleOwner, downSyncStatusObserver)
    }

    private fun observeAndUpdateLatestUpSyncTime() {

        val upSyncObserver = Observer<UpSyncStatus> {
            latestUpSyncTime = it.lastUpSyncTime
            calculateLatestSyncTimeIfPossible(latestDownSyncTime, latestUpSyncTime)
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

        val downSyncObserver = Observer<MutableList<WorkStatus>> {
            if (it.size > 0) {
                val currentSyncState = it.last().state
                if(syncState != currentSyncState) {
                    syncState = currentSyncState
                    updateSyncInfo()
                    updateCardView()
                }
            }
        }
        syncStatusViewModel.downSyncWorkStatus.observe(lifecycleOwner, downSyncObserver)
    }

    private fun updateCardView() = cardView?.bind(this)

    class SyncStatusViewModel(
        syncStatusDbModel: SyncStatusDao,
        projectId: String,
        getWorkManager: () -> WorkManager = WorkManager::getInstance): ViewModel() {

        val syncStatus = syncStatusDbModel.getSyncStatus()

        val downSyncWorkStatus =
            getWorkManager().getStatusesForUniqueWork(
                "$projectId-${PeopleDownSyncMaster.DOWN_SYNC_WORK_NAME_SUFFIX}")
    }

    class NewSyncStatusViewModel(
        downSyncDbModel: DownSyncDao,
        upSyncDbModel: UpSyncDao) {

        val downSyncStatus = downSyncDbModel.getDownSyncStatus()

        val upSyncStatus = upSyncDbModel.getUpSyncStatus()
    }
}
