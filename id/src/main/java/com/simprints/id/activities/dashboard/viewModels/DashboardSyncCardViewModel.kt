package com.simprints.id.activities.dashboard.viewModels

import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State.RUNNING
import androidx.work.WorkManager
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
                                 private val lifecycleOwner: LifecycleOwner,
                                 component: AppComponent,
                                 defaultState: State = State()) : CardViewModel(type, position) {

    @Inject
    lateinit var syncStatusDatabase: SyncStatusDatabase
    @Inject
    lateinit var syncScopesBuilder: SyncScopesBuilder
    private val syncScope: SyncScope?
        get() = syncScopesBuilder.buildSyncScope()

    private val helper = DashboardSyncCardViewModelHelper(this)

    val stateLiveData: MutableLiveData<State> = MutableLiveData()
    var viewModelState: State = State()

    data class State(var onSyncActionClicked: () -> Unit = {},
                     var peopleToUpload: Int = 0,
                     var peopleToDownload: Int? = null,
                     var peopleInDb: Int = 0,
                     var isDownSyncRunning: Boolean = false,
                     val showSyncButton: Boolean = false,
                     var lastSyncTime: String = "")

    private var downSyncStatus: LiveData<List<DownSyncStatus>>
    private var upSyncStatus: LiveData<UpSyncStatus?>
    private var downSyncWorkerStatus: LiveData<MutableList<WorkInfo>>?

    init {
        component.inject(this)

        viewModelState = defaultState

        downSyncStatus = syncStatusDatabase.downSyncDao.getDownSyncStatusLiveData()
        upSyncStatus = syncStatusDatabase.upSyncDao.getUpSyncStatus()
        downSyncWorkerStatus = syncScope?.let { WorkManager.getInstance().getWorkInfosByTagLiveData(SUBDOWNSYNC_WORKER_TAG) }

        helper.initViewModel(component)
    }

    fun registerObserverForDownSyncWorkerState() {
        val downSyncObserver = Observer<List<WorkInfo>> { subWorkersStatusInSyncChain ->
            val isAnyOfWorkersInSyncChainRunning = subWorkersStatusInSyncChain.firstOrNull { it.state == RUNNING } != null
            helper.onDownSyncWorkerStatusChange(isAnyOfWorkersInSyncChainRunning)
        }
        downSyncWorkerStatus?.observe(lifecycleOwner, downSyncObserver)
    }

    fun registerObserverForDownSyncEvents() {

        val downSyncStatusObserver = Observer<List<DownSyncStatus>> { downSyncStatuses ->
            helper.onDownSyncStatusChanged(downSyncStatuses)
        }
        downSyncStatus.observe(lifecycleOwner, downSyncStatusObserver)
    }

    fun registerObserverForUpSyncEvents() {

        val upSyncObserver = Observer<UpSyncStatus?> { upSyncStatus ->
            helper.onUpSyncStatusChanged(upSyncStatus)
        }
        upSyncStatus.observe(lifecycleOwner, upSyncObserver)
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
}
