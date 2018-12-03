package com.simprints.id.activities.dashboard.viewModels

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State.RUNNING
import androidx.work.WorkManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.room.DownSyncStatus
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

class DashboardSyncCardViewModel(override val type: DashboardCardType,
                                 override val position: Int,
                                 private val lifecycleOwner: LifecycleOwner,
                                 val component: AppComponent,
                                 defaultState: State = State()): CardViewModel(type, position) {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var localDbManager: LocalDbManager
    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase
    @Inject lateinit var syncScopesBuilder: SyncScopesBuilder
    @Inject lateinit var downSyncManager: DownSyncManager

    val stateLiveData: MutableLiveData<State> = MutableLiveData()
    var viewModelState: State = State()
        set(value) {
            field = value
            stateLiveData.value = value
        }

    init {
        viewModelState = defaultState
    }

    data class State(var onSyncActionClicked: () -> Unit = {},
                     var peopleToUpload: Int = 0,
                     var peopleToDownload: Int = 0,
                     var peopleInDb: Int = 0,
                     var isSyncRunning: Boolean = false,
                     var lastSyncTime: String = "")


    private val syncScope: SyncScope?
        get() = syncScopesBuilder.buildSyncScope()

    private val dateFormat: DateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault())
    }

    private var latestDownSyncTime: Long? = null
    private var latestUpSyncTime: Long? = null

    private var downSyncStatus: LiveData<List<DownSyncStatus>>
    private var upSyncStatus: LiveData<UpSyncStatus?>
    private var downSyncWorkerStatus: LiveData<MutableList<WorkInfo>>?

    init {
        component.inject(this)

        downSyncStatus = syncStatusDatabase.downSyncDao.getDownSyncStatus()
        upSyncStatus = syncStatusDatabase.upSyncDao.getUpSyncStatus()
        downSyncWorkerStatus = syncScope?.let { WorkManager.getInstance().getWorkInfosByTagLiveData(SYNC_WORKER_TAG) }

        initViewModel()
    }

    private fun initViewModel() {
        val isSyncRunning = downSyncManager.isDownSyncRunning()
        if (isSyncRunning) {
            registerObservers()
            updateSyncInfo()
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
            }, onError = {
                registerObservers()
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
            var peopleToDownload = 0
            syncScope?.toSubSyncScopes()?.forEach { it ->
                peopleToDownload += dbManager.calculateNPatientsToDownSync(it.projectId, it.userId, it.moduleId).blockingGet()
            }
            viewModelState = viewModelState.copy(peopleToDownload = peopleToDownload)
        }

    private fun updateTotalLocalPeopleCount(): Completable =
        dbManager.getPeopleCountFromLocalForSyncGroup(preferencesManager.syncGroup)
            .flatMapCompletable {
                viewModelState = viewModelState.copy(peopleInDb = it)
                Completable.complete()
            }

    private fun updateLocalPeopleToUpSyncCount(): Completable =
        localDbManager.getPeopleCountFromLocal(toSync = true)
            .flatMapCompletable {
                viewModelState = viewModelState.copy(peopleToUpload = it)
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
                if (viewModelState.isSyncRunning) {
                    viewModelState = viewModelState.copy(peopleToDownload = peopleToDownSync)
                }
                latestDownSyncTime = latestDownSyncTimeForModules.max()
                val lastSyncTime = calculateLatestSyncTimeIfPossible(latestDownSyncTime, latestUpSyncTime)
                viewModelState = viewModelState.copy(lastSyncTime = lastSyncTime)
            }
        }
        downSyncStatus.observe(lifecycleOwner, downSyncStatusObserver)
    }

    private fun observeAndUpdateLatestUpSyncTime() {

        val upSyncObserver = Observer<UpSyncStatus?> {
            latestUpSyncTime = it?.lastUpSyncTime
            val lastSyncTime = calculateLatestSyncTimeIfPossible(latestDownSyncTime, latestUpSyncTime)
            viewModelState = viewModelState.copy(lastSyncTime = lastSyncTime)

        }
        upSyncStatus.observe(lifecycleOwner, upSyncObserver)
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
            }, onError = {
                it.printStackTrace()
            })
    }

    private fun observeDownSyncStatusAndUpdateButton() {

        val downSyncObserver = Observer<List<WorkInfo>> { subWorkersStatusInSyncChain ->
            val isAnyOfWorkersInSyncChainRunning = subWorkersStatusInSyncChain.firstOrNull { it.state == RUNNING } != null

            if (viewModelState.isSyncRunning != isAnyOfWorkersInSyncChainRunning) {
                viewModelState = viewModelState.copy(isSyncRunning = isAnyOfWorkersInSyncChainRunning)
                if (!viewModelState.isSyncRunning) {
                    updateSyncInfo()
                }
            }
        }
        downSyncWorkerStatus?.observe(lifecycleOwner, downSyncObserver)
    }
}
