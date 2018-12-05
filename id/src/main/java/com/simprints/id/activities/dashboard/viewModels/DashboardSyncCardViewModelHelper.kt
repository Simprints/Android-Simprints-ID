package com.simprints.id.activities.dashboard.viewModels

import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.room.DownSyncStatus
import com.simprints.id.data.db.local.room.UpSyncStatus
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.peopleDownSync.SyncStatusDatabase
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.PeopleDownSyncTrigger
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

class DashboardSyncCardViewModelHelper(private val viewModel: DashboardSyncCardViewModel) {

    enum class HelperState {
        NEED_INITIALIZATION,
        INITIALIZING,
        READY
    }

    private fun HelperState.isInReady() = this == HelperState.READY
    private fun HelperState.isInNeedInit() = this == HelperState.NEED_INITIALIZATION

    @Inject
    lateinit var preferencesManager: PreferencesManager
    @Inject
    lateinit var loginInfoManager: LoginInfoManager
    @Inject
    lateinit var dbManager: DbManager
    @Inject
    lateinit var remoteDbManager: RemoteDbManager
    @Inject
    lateinit var localDbManager: LocalDbManager
    @Inject
    lateinit var syncStatusDatabase: SyncStatusDatabase
    @Inject
    lateinit var syncScopesBuilder: SyncScopesBuilder

    private var helperState: HelperState = HelperState.NEED_INITIALIZATION

    private val state: DashboardSyncCardViewModel.State
        get() = viewModel.viewModelState

    private val syncScope: SyncScope?
        get() = syncScopesBuilder.buildSyncScope()

    private var latestDownSyncTime: Long? = null
    private var latestUpSyncTime: Long? = null

    private val dateFormat: DateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault())
    }

    fun initViewModel(component: AppComponent) {
        component.inject(this)
        viewModel.registerObserverForDownSyncWorkerState()
    }

    private fun updateTotalDownSyncCount(): Completable =
        Completable.fromAction {
            try {
                var peopleToDownload = 0
                syncScope?.toSubSyncScopes()?.forEach { it ->
                    peopleToDownload += dbManager.calculateNPatientsToDownSync(it.projectId, it.userId, it.moduleId).blockingGet()
                }
                viewModel.updateState(peopleToDownload = peopleToDownload, emitState = helperState.isInReady())
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }

    private fun updateTotalLocalCount(): Completable =
        dbManager.getPeopleCountFromLocalForSyncGroup(preferencesManager.syncGroup)
            .flatMapCompletable {
                viewModel.updateState(peopleInDb = it, emitState = true)
                Completable.complete()
            }

    private fun updateTotalUpSyncCount(): Completable =
        localDbManager.getPeopleCountFromLocal(toSync = true)
            .flatMapCompletable {
                viewModel.updateState(peopleToUpload = it, emitState = true)
                Completable.complete()
            }

    fun onUpSyncStatusChanged(upSyncStatus: UpSyncStatus?) {
        upSyncStatus?.let {
            latestUpSyncTime = it.lastUpSyncTime
            val lastSyncTime = calculateLatestSyncTimeIfPossible(latestDownSyncTime, latestUpSyncTime)
            viewModel.updateState(lastSyncTime = lastSyncTime, emitState = helperState.isInReady())
            fetchLocalAndUpSyncCounters()
        }
    }

    fun onDownSyncStatusChanged(downSyncStatuses: List<DownSyncStatus>) {
        if (downSyncStatuses.isNotEmpty()) {

            var peopleToDownSync: Int? = null
            if(state.isDownSyncRunning) {
                peopleToDownSync = updateTotalDownSyncCountUsingWorkers(downSyncStatuses)
            }

            val latestDownSyncTime = updateLatestDownSyncTime(downSyncStatuses)
            viewModel.updateState(
                lastSyncTime = latestDownSyncTime,
                peopleToDownload = peopleToDownSync,
                emitState = helperState.isInReady())
        }
    }

    private fun updateLatestDownSyncTime(downSyncStatuses: List<DownSyncStatus>): String? =
        if (downSyncStatuses.isNotEmpty()) {
            val latestDownSyncTimeForModules: ArrayList<Long> = ArrayList()
            syncScope?.let {
                it.toSubSyncScopes().forEach { subSyncScope ->
                    val downSyncStatusForSubScope = filterDownSyncStatusesBySubSyncScope(subSyncScope, downSyncStatuses)
                    downSyncStatusForSubScope?.lastSyncTime?.let{ lastSyncTime -> latestDownSyncTimeForModules.add(lastSyncTime) }
                }
            }
            latestDownSyncTime = latestDownSyncTimeForModules.max()
            calculateLatestSyncTimeIfPossible(latestDownSyncTime, latestUpSyncTime)
        } else {
            null
        }

    private fun updateTotalDownSyncCountUsingWorkers(downSyncStatuses: List<DownSyncStatus>): Int? =
        if (downSyncStatuses.isNotEmpty()) {
            var peopleToDownSync = 0
            syncScope?.let {
                it.toSubSyncScopes().forEach { subSyncScope ->
                    peopleToDownSync += filterDownSyncStatusesBySubSyncScope(subSyncScope, downSyncStatuses)?.totalToDownload ?: 0
                }
            }
            peopleToDownSync
        } else {
            null
        }

    private fun filterDownSyncStatusesBySubSyncScope(subSyncScope: SubSyncScope, downSyncStatuses: List<DownSyncStatus>): DownSyncStatus? {
        with(subSyncScope) {
            return downSyncStatuses.findLast {
                it.projectId == this.projectId &&
                it.userId == this.userId &&
                it.moduleId == this.moduleId
            }
        }
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

    fun onDownSyncWorkerStatusChange(isDownSyncRunning: Boolean) {
        if (state.isDownSyncRunning != isDownSyncRunning || helperState.isInNeedInit()) {
            state.isDownSyncRunning = isDownSyncRunning
            if (isDownSyncRunning) {
                initWhenDownSyncRunningIfRequired()
            } else {
                initWhenDownSyncNotRunningIfRequired()
                fetchLocalAndUpSyncCounters()
            }
            viewModel.updateState(isDownSyncRunning = isDownSyncRunning, emitState = helperState.isInReady())
        }
    }

    private fun initWhenDownSyncNotRunningIfRequired() {
        if (helperState == HelperState.NEED_INITIALIZATION) {
            helperState = HelperState.INITIALIZING
            viewModel.updateState(showSyncButton = preferencesManager.peopleDownSyncTriggers[PeopleDownSyncTrigger.MANUAL], emitState = helperState.isInReady())
            fetchAllCounters {
                setHelperInitialized()
            }
        }
    }

    private fun initWhenDownSyncRunningIfRequired() {
        if (helperState == HelperState.NEED_INITIALIZATION) {
            helperState = HelperState.INITIALIZING
            viewModel.updateState(showSyncButton = preferencesManager.peopleDownSyncTriggers[PeopleDownSyncTrigger.MANUAL], emitState = helperState.isInReady())
            fetchLocalAndUpSyncCounters {
                setHelperInitialized()
            }
        }
    }

    private fun fetchAllCounters(done: () -> Unit) {
        updateTotalDownSyncCount()
            .andThen(updateTotalLocalCount())
            .andThen(updateTotalUpSyncCount())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .doFinally {
                done()
            }
            .subscribeBy(onComplete = {
            }, onError = {
                it.printStackTrace()
            })
    }

    private fun fetchLocalAndUpSyncCounters(done: () -> Unit = {}) {
        updateTotalLocalCount()
            .andThen(updateTotalUpSyncCount())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .doFinally {
                done()
            }
            .subscribeBy(onComplete = {
            }, onError = { it.printStackTrace() })
    }

    private fun setHelperInitialized() {
        helperState = HelperState.READY
        viewModel.registerObserverForDownSyncEvents()
        viewModel.registerObserverForUpSyncEvents()
        viewModel.updateState(emitState = true)
    }
}
