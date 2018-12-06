package com.simprints.id.activities.dashboard.viewModels.syncCard

import android.os.Looper
import androidx.lifecycle.*
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State.RUNNING
import androidx.work.WorkManager
import com.simprints.id.activities.dashboard.viewModels.CardViewModel
import com.simprints.id.activities.dashboard.viewModels.DashboardCardType
import com.simprints.id.data.db.local.room.DownSyncStatus
import com.simprints.id.data.db.local.room.UpSyncStatus
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.peopleDownSync.SyncStatusDatabase
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.ConstantsWorkManager.Companion.SUBDOWNSYNC_WORKER_TAG
import javax.inject.Inject

class DashboardSyncCardViewModel(override val type: DashboardCardType,
                                 override val position: Int,
                                 val component: AppComponent,
                                 defaultState: DashboardSyncCardViewModelState = DashboardSyncCardViewModelState()): CardViewModel(type, position), LifecycleOwner {

    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase
    @Inject lateinit var syncScopesBuilder: SyncScopesBuilder

    private val syncScope: SyncScope?
        get() = syncScopesBuilder.buildSyncScope()

    private var helper: DashboardSyncCardViewModelHelper? = null

    val stateLiveData: MutableLiveData<DashboardSyncCardViewModelState> = MutableLiveData()
    var viewModelState: DashboardSyncCardViewModelState = DashboardSyncCardViewModelState()

    private var downSyncStatus: LiveData<List<DownSyncStatus>>
    private var upSyncStatus: LiveData<UpSyncStatus?>
    private var downSyncWorkerStatus: LiveData<MutableList<WorkInfo>>?
    var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    init {
        component.inject(this)

        viewModelState = defaultState

        downSyncStatus = syncStatusDatabase.downSyncDao.getDownSyncStatusLiveData()
        upSyncStatus = syncStatusDatabase.upSyncDao.getUpSyncStatus()
        downSyncWorkerStatus = syncScope?.let { WorkManager.getInstance().getWorkInfosByTagLiveData(SUBDOWNSYNC_WORKER_TAG) }

        registerObserverForDownSyncWorkerState()
        lifecycleRegistry.markState(Lifecycle.State.STARTED)
    }

    private fun registerObserverForDownSyncWorkerState() {
        val downSyncObserver = Observer<List<WorkInfo>> { subWorkersStatusInSyncChain ->
            val isDownSyncRunning = subWorkersStatusInSyncChain.firstOrNull { it.state == RUNNING } != null
            if (helper == null) {
                helper = DashboardSyncCardViewModelHelper(this, component, isDownSyncRunning)
            } else {
                helper?.onDownSyncWorkerStatusChange(isDownSyncRunning)
            }
        }
        downSyncWorkerStatus?.observe(this, downSyncObserver)
    }

    fun registerObserverForDownSyncEvents() {

        val downSyncStatusObserver = Observer<List<DownSyncStatus>> { downSyncStatuses ->
            helper?.onDownSyncStatusChanged(downSyncStatuses)
        }
        downSyncStatus.observe(this, downSyncStatusObserver)
    }

    fun registerObserverForUpSyncEvents() {

        val upSyncObserver = Observer<UpSyncStatus?> { upSyncStatus ->
            helper?.onUpSyncStatusChanged(upSyncStatus)
        }
        upSyncStatus.observe(this, upSyncObserver)
    }

    fun updateState(peopleToUpload: Int? = null,
                    peopleToDownload: Int? = null,
                    peopleInDb: Int? = null,
                    isDownSyncRunning: Boolean? = null,
                    lastSyncTime: String? = null,
                    showSyncButton: Boolean? = null,
                    emitState: Boolean = false) {
        viewModelState = viewModelState.let {
            it.copy(
                peopleToUpload = peopleToUpload ?: it.peopleToUpload,
                peopleToDownload = peopleToDownload ?: it.peopleToDownload,
                peopleInDb = peopleInDb ?: it.peopleInDb,
                isDownSyncRunning = isDownSyncRunning ?: it.isDownSyncRunning,
                lastSyncTime = lastSyncTime ?: it.lastSyncTime,
                showSyncButton = showSyncButton ?: it.showSyncButton
            )
        }

        if (emitState) {
            emitState()
        }
    }

    private fun emitState() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            stateLiveData.value = viewModelState
        } else {
            stateLiveData.postValue(viewModelState)
        }
    }

    override fun stopObservers(){
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED)
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}
