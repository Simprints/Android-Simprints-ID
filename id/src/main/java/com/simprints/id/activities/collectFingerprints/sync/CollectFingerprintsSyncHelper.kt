package com.simprints.id.activities.collectFingerprints.sync

import android.app.Activity
import android.content.Context
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.collectFingerprints.CollectFingerprintsContract
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


class CollectFingerprintsSyncHelper(private val context: Context,
                                    private val view: CollectFingerprintsContract.View) {

    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var syncManager: SyncManager

    init {
        ((view as Activity).application as Application).component.inject(this)

        setReadySyncItem()
    }

    fun startListeners() {
        syncManager.addObserver(collectFingerprintsSyncObserver())
    }

    fun stopListeners() {
        syncManager.removeObservers()
    }

    private fun collectFingerprintsSyncObserver(): DisposableObserver<Progress> =
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

    fun sync() {
        setZeroProgressSyncItem()
        syncManager.sync(SyncTaskParameters.build(preferencesManager.syncGroup, preferencesManager.moduleId, loginInfoManager))
    }

    private fun setProgressSyncItem(progress: Progress) {
        if (isProgressZero(progress))
            setZeroProgressSyncItem()
        else setSyncItem(false,
            context.getString(R.string.syncing_with_progress, progress.currentValue, progress.maxValue),
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
        setSyncItem(enabled, context.getString(title), icon)
    }

    private fun setSyncItem(enabled: Boolean, title: String, @DrawableRes icon: Int) {
        (view as Activity).runOnUiThreadIfStillRunning {
            view.syncItem.isEnabled = enabled
            view.syncItem.title = title
            view.syncItem.setIcon(icon)
        }
    }
}
