package com.simprints.id.activities.collectFingerprints

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.view.MenuItem
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.DataManager
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.sync.SyncManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning
import io.reactivex.observers.DisposableObserver
import timber.log.Timber
import javax.inject.Inject

// Because who in their right mind would want to write in Java
class MainActivitySyncHelper(private val activity: MainActivity,
                             private val syncItem: MenuItem) {

    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var syncManager: SyncManager

    init {
        (activity.application as Application).component.inject(this)

        syncManager.addObserver(mainActivitySyncObserver())
        setReadySyncItem()
    }

    private fun mainActivitySyncObserver(): DisposableObserver<Progress> =
        object : DisposableObserver<Progress>() {

            override fun onNext(progress: Progress) {
                Timber.d("Sync in progress update : " + progress.currentValue + " / " + progress.maxValue)
                setProgressSyncItem(progress)
            }

            override fun onError(t: Throwable) {
                Timber.d("Sync failed")
                analyticsManager.logThrowable(t)
                setErrorSyncItem()
            }

            override fun onComplete() {
                Timber.d("Sync complete")
                setCompleteSyncItem()
            }
        }

    fun sync(dataManager: DataManager) {
        setZeroProgressSyncItem()
        syncManager.sync(SyncTaskParameters.build(preferencesManager.syncGroup, preferencesManager.moduleId, loginInfoManager))
    }

    private fun setProgressSyncItem(progress: Progress) {
        if (isProgressZero(progress))
            setZeroProgressSyncItem()
        else setSyncItem(false,
                activity.getString(R.string.syncing_with_progress, progress.currentValue, progress.maxValue),
                R.drawable.ic_syncing)
    }

    private fun setZeroProgressSyncItem() {
        setSyncItem(false, R.string.syncing_calculating, R.drawable.ic_syncing)
    }

    private fun isProgressZero(progress: Progress): Boolean {
        return progress.currentValue == 0 && progress.maxValue == 0
    }

    private fun setCompleteSyncItem() {
        setSyncItem(true, R.string.nav_sync_complete, R.drawable.ic_sync_success)
    }

    private fun setReadySyncItem() {
        setSyncItem(true, R.string.nav_sync, R.drawable.ic_menu_sync_ready)
    }

    private fun setErrorSyncItem() {
        setSyncItem(true, R.string.nav_sync_failed, R.drawable.ic_menu_sync_bad)
    }

    private fun setSyncItem(enabled: Boolean, @StringRes title: Int, @DrawableRes icon: Int) {
        setSyncItem(enabled, activity.getString(title), icon)
    }

    private fun setSyncItem(enabled: Boolean, title: String, @DrawableRes icon: Int) {
        activity.runOnUiThreadIfStillRunning {
            syncItem.isEnabled = enabled
            syncItem.title = title
            syncItem.setIcon(icon)
        }
    }
}
