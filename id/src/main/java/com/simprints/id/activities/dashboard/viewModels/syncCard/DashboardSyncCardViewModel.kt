package com.simprints.id.activities.dashboard.viewModels.syncCard

import android.os.Looper
import androidx.lifecycle.*
import com.simprints.id.activities.dashboard.viewModels.CardViewModel
import com.simprints.id.activities.dashboard.viewModels.DashboardCardType
import com.simprints.id.data.db.local.room.DownSyncStatus
import com.simprints.id.data.db.local.room.UpSyncStatus
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.peopleDownSync.SyncStatusDatabase
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManager
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncState
import javax.inject.Inject

class DashboardSyncCardViewModel(override val type: DashboardCardType,
                                 override val position: Int,
                                 val component: AppComponent,
                                 private val downSyncStatus: LiveData<List<DownSyncStatus>>,
                                 private val upSyncStatus: LiveData<UpSyncStatus?>,
                                 defaultState: DashboardSyncCardViewModelState = DashboardSyncCardViewModelState()) : CardViewModel(type, position), LifecycleOwner {

    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase
    @Inject lateinit var syncScopesBuilder: SyncScopesBuilder
    @Inject lateinit var downSyncManager: DownSyncManager
    private var lastSyncState: SyncState = SyncState.NOT_RUNNING

    var helper: DashboardSyncCardViewModelHelper? = null

    val stateLiveData: MutableLiveData<DashboardSyncCardViewModelState> = MutableLiveData()
    var viewModelState: DashboardSyncCardViewModelState = DashboardSyncCardViewModelState()

    private var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    init {
        component.inject(this)
        viewModelState = defaultState
        registerObserverForDownSyncWorkerState()
        lifecycleRegistry.markState(Lifecycle.State.STARTED)
    }

    private fun registerObserverForDownSyncWorkerState() {
        val downSyncObserver = Observer<SyncState> { syncState ->
            if (helper == null) {
                lastSyncState = syncState
                helper = DashboardSyncCardViewModelHelper(this, component, syncState == SyncState.RUNNING)
            } else if (lastSyncState != syncState) {
                helper?.onSyncStateChange(syncState)
            }

            lastSyncState = syncState
        }

        downSyncManager.onSyncStateUpdated().observe(this, downSyncObserver)
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
                    syncCardState: SyncCardState? = null,
                    lastSyncTime: String? = null,
                    emitState: Boolean = false) {
        viewModelState = viewModelState.let {
            it.copy(
                peopleToUpload = peopleToUpload ?: it.peopleToUpload,
                peopleToDownload = peopleToDownload ?: it.peopleToDownload,
                peopleInDb = peopleInDb ?: it.peopleInDb,
                syncCardState = syncCardState ?: it.syncCardState,
                lastSyncTime = lastSyncTime ?: it.lastSyncTime
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

    override fun stopObservers() {
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED)
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    fun areThereRecordsToSync(): Boolean =
        viewModelState.peopleToUpload?.let { it > 0 } ?: false ||
            viewModelState.peopleToDownload?.let { it > 0 } ?: false
}
