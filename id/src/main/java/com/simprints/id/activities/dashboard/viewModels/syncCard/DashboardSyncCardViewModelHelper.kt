package com.simprints.id.activities.dashboard.viewModels.syncCard

import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.room.DownSyncStatus
import com.simprints.id.data.db.local.room.UpSyncStatus
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.SyncSchedulerHelper
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

class DashboardSyncCardViewModelHelper(private val viewModel: DashboardSyncCardViewModel,
                                       component: AppComponent,
                                       isDownSyncRunning: Boolean) {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var localDbManager: LocalDbManager
    @Inject lateinit var syncScopesBuilder: SyncScopesBuilder
    @Inject lateinit var syncSchedulerHelper: SyncSchedulerHelper

    private val state: DashboardSyncCardViewModelState
        get() = viewModel.viewModelState

    private val syncScope: SyncScope?
        get() = syncScopesBuilder.buildSyncScope()

    private var latestDownSyncTime: Long? = null
    private var latestUpSyncTime: Long? = null

    val dateFormat: DateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault())
    }

    init {
        component.inject(this)

        state.showRunningStateForSyncButton = isDownSyncRunning
        state.showSyncButton = syncSchedulerHelper.isDownSyncManualTriggerOn()

        if (isDownSyncRunning) {
            initWhenDownSyncRunningIfRequired()
        } else {
            initWhenDownSyncNotRunningIfRequired()
        }
    }

    private fun initWhenDownSyncNotRunningIfRequired() {
        fetchAllCounters {
            setHelperInitialized()
        }
    }

    private fun initWhenDownSyncRunningIfRequired() {
        fetchLocalAndUpSyncCounters {
            setHelperInitialized()
        }
    }

    private fun fetchAllCounters(done: () -> Unit) {
        Completable.mergeArray(updateTotalDownSyncCount(), updateTotalLocalCount(), updateTotalUpSyncCount())
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
        viewModel.registerObserverForDownSyncEvents()
        viewModel.registerObserverForUpSyncEvents()
        viewModel.updateState(emitState = true)
    }

    private fun updateTotalDownSyncCount(): Completable =
        Completable.fromAction {
            try {
                var peopleToDownload = 0
                syncScope?.toSubSyncScopes()?.forEach { it ->
                    peopleToDownload += dbManager.calculateNPatientsToDownSync(it.projectId, it.userId, it.moduleId).blockingGet()
                }
                viewModel.updateState(peopleToDownload = peopleToDownload, emitState = true)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }.subscribeOn(Schedulers.io())

    private fun updateTotalLocalCount(): Completable =
        dbManager.getPeopleCountFromLocalForSyncGroup(preferencesManager.syncGroup)
            .flatMapCompletable {
                viewModel.updateState(peopleInDb = it, emitState = true)
                Completable.complete()
            }.subscribeOn(Schedulers.io())

    private fun updateTotalUpSyncCount(): Completable =
        localDbManager.getPeopleCountFromLocal(toSync = true)
            .flatMapCompletable {
                viewModel.updateState(peopleToUpload = it, emitState = true)
                Completable.complete()
            }.subscribeOn(Schedulers.io())

    fun onUpSyncStatusChanged(upSyncStatus: UpSyncStatus?) {
        upSyncStatus?.let {
            latestUpSyncTime = it.lastUpSyncTime
            val lastSyncTime = calculateLatestSyncTimeIfPossible(latestDownSyncTime, latestUpSyncTime)
            viewModel.updateState(lastSyncTime = lastSyncTime, emitState = true)
            fetchLocalAndUpSyncCounters()
        }
    }

    fun onDownSyncStatusChanged(downSyncStatuses: List<DownSyncStatus>) {
        if (downSyncStatuses.isNotEmpty()) {

            var peopleToDownSync: Int? = null
            if (state.showRunningStateForSyncButton) {
                peopleToDownSync = updateTotalDownSyncCountUsingWorkers(downSyncStatuses)
            }

            val latestDownSyncTime = updateLatestDownSyncTime(downSyncStatuses)
            viewModel.updateState(
                lastSyncTime = latestDownSyncTime,
                peopleToDownload = peopleToDownSync,
                emitState = true)
        }
    }

    private fun updateLatestDownSyncTime(downSyncStatuses: List<DownSyncStatus>): String? =
        if (downSyncStatuses.isNotEmpty()) {
            val latestDownSyncTimeForModules: ArrayList<Long> = ArrayList()
            syncScope?.let {
                it.toSubSyncScopes().forEach { subSyncScope ->
                    val downSyncStatusForSubScope = filterDownSyncStatusesBySubSyncScope(subSyncScope, downSyncStatuses)
                    downSyncStatusForSubScope?.lastSyncTime?.let { lastSyncTime -> latestDownSyncTimeForModules.add(lastSyncTime) }
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
                dateFormat.format(lastDownSyncDate)
            } else {
                dateFormat.format(lastUpSyncDate)
            }
        }

        lastDownSyncDate?.let { return dateFormat.format(it) }
        lastUpSyncDate?.let { return dateFormat.format(it) }
        return ""
    }

    fun onDownSyncWorkerStatusChange(isDownSyncRunning: Boolean) {
        if (state.showRunningStateForSyncButton != isDownSyncRunning) {
            state.showRunningStateForSyncButton = isDownSyncRunning
            viewModel.updateState(isDownSyncRunning = isDownSyncRunning, emitState = state.peopleToDownload != null)
            if(!state.showRunningStateForSyncButton) {
                updateTotalLocalCount().subscribeBy(onError = {it.printStackTrace()}, onComplete = {})
            }
        }
    }
}
