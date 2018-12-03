package com.simprints.id.activities.dashboard.models

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State.RUNNING
import androidx.work.WorkManager
import com.simprints.id.activities.dashboard.views.DashboardSyncCardView
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.room.DownSyncDao
import com.simprints.id.data.db.local.room.DownSyncStatus
import com.simprints.id.data.db.local.room.UpSyncDao
import com.simprints.id.data.db.local.room.UpSyncStatus
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.peopleDownSync.SyncStatusDatabase
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManager
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.ConstantsWorkManager.Companion.SYNC_WORKER_TAG
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
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
    @Inject lateinit var newSyncStatusDatabase: SyncStatusDatabase
    @Inject lateinit var syncScopesBuilder: SyncScopesBuilder

    private var masterSync: DownSyncManager

    private var newSyncStatusViewModel: NewSyncStatusViewModel

    private val syncScope: SyncScope?
        get() = syncScopesBuilder.buildSyncScope()

    private val dateFormat: DateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault())
    }

    var cardView: DashboardSyncCardView? = null

    private var latestDownSyncTime: Long? = null
    private var latestUpSyncTime: Long? = null

    var onSyncActionClicked: (cardModel: DashboardSyncCardViewModel) -> Unit = {}
    var peopleToUpload = 0
    var peopleToDownload = 0
    var peopleInDb = 0
    var isSyncRunning = false

    var lastSyncTime = ""

    init {
        component.inject(this)
        masterSync = DownSyncManager(syncScopesBuilder)

        newSyncStatusViewModel = NewSyncStatusViewModel(
            newSyncStatusDatabase.downSyncDao,
            newSyncStatusDatabase.upSyncDao,
            syncScope)

        initViewModel()
    }

    private fun initViewModel() {
        isSyncRunning = masterSync.isDownSyncRunning()
        if (isSyncRunning) {
            registerObservers()
            updateSyncInfo()
            updateCardView()
        } else {
            fetchDownSyncCounterAndThenRegisterObservers()
        }
    }

    private fun fetchDownSyncCounterAndThenRegisterObservers() {
        updateTotalPeopleToDownSyncCount()
            .andThen(updateTotalLocalPeopleCount())
            .andThen(updateLocalPeopleToUpSyncCount())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribeBy(onComplete = {
                registerObservers()
                updateCardView()
            }, onError = {
                registerObservers()
                updateCardView()
                it.printStackTrace()
            })
    }

    private fun registerObservers() {
        observeAndUpdatePeopleToDownloadAndLatestDownSyncTimes()
        observeAndUpdateLatestUpSyncTime()
        observeDownSyncStatusAndUpdateButton()
    }

    private fun updateTotalPeopleToDownSyncCount(): Completable =
        Completable.fromAction {
            peopleToDownload = 0
            syncScope?.toSubSyncScopes()?.forEach { it ->
                peopleToDownload += dbManager.calculateNPatientsToDownSync(it.projectId, it.userId, it.moduleId).blockingGet()
            }
        }

    private fun updateTotalLocalPeopleCount(): Completable =
        dbManager.getPeopleCountFromLocalForSyncGroup(preferencesManager.syncGroup)
            .flatMapCompletable {
                peopleInDb = it
                Completable.complete()
            }

    private fun updateLocalPeopleToUpSyncCount(): Completable =
        localDbManager.getPeopleCountFromLocal(toSync = true)
            .flatMapCompletable {
                peopleToUpload = it
                Completable.complete()
            }

    private fun observeAndUpdatePeopleToDownloadAndLatestDownSyncTimes() {

        val downSyncStatusObserver = Observer<List<DownSyncStatus>> { downSyncStatuses ->
            if (downSyncStatuses.isNotEmpty()) {
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

    private fun updateSyncInfo() {
        updateTotalLocalPeopleCount()
            .andThen(updateLocalPeopleToUpSyncCount())
            .subscribeBy(onComplete = {
                updateCardView()
            }, onError = {
                updateCardView()
                it.printStackTrace()
            })
    }

    private fun observeDownSyncStatusAndUpdateButton() {

        val downSyncObserver = Observer<List<WorkInfo>> { subWorkersStatusInSyncChain ->
            val isAnyOfWorkersInSyncChainRunning = subWorkersStatusInSyncChain.firstOrNull { it.state == RUNNING } != null

            if (isSyncRunning != isAnyOfWorkersInSyncChainRunning) {
                isSyncRunning = isAnyOfWorkersInSyncChainRunning
                updateCardView()
                if (!isSyncRunning) {
                    updateSyncInfo()
                }
            }
        }
        newSyncStatusViewModel.downSyncWorkerStatus?.observe(lifecycleOwner, downSyncObserver)
    }

    private fun updateCardView() = cardView?.bind(this)

    class NewSyncStatusViewModel(
        downSyncDbModel: DownSyncDao,
        upSyncDbModel: UpSyncDao,
        syncScope: SyncScope?) {

        val downSyncStatus = downSyncDbModel.getDownSyncStatus()
        val upSyncStatus = upSyncDbModel.getUpSyncStatus()
        val downSyncWorkerStatus = syncScope?.let { WorkManager.getInstance().getWorkInfosByTagLiveData(SYNC_WORKER_TAG) }
    }
}
