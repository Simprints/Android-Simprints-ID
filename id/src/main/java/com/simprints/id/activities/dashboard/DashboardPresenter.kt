package com.simprints.id.activities.dashboard

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.simprints.id.R
import com.simprints.id.data.DataManager
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.id.data.db.sync.NaiveSyncManager
import com.simprints.id.domain.Constants.GROUP
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.progress.UploadProgress
import com.simprints.id.services.sync.SyncClient
import io.reactivex.observers.DisposableObserver

class DashboardPresenter(private val view: DashboardContract.View,
                         syncClient: SyncClient,
                         private val dataManager: DataManager) : DashboardContract.Presenter {

    private var started: Boolean = false
    private val syncManager = NaiveSyncManager(dataManager, syncClient, object : DisposableObserver<Progress>() {

        override fun onNext(progress: Progress) {
            setProgressSyncItem(progress)
        }

        override fun onComplete() {
            setCompleteSyncItem()
        }

        override fun onError(throwable: Throwable) {
            setErrorSyncItem()
        }
    })


    override fun start() {
        if (!started) {
            started = true

            //FIXME: remove it!
            val realm = dataManager.getRealmInstance()
            realm.executeTransaction {
                it.where(rl_Person::class.java).findAll().deleteAllFromRealm()
            }
        }
    }

    override fun pause() {
        syncManager.stop()
    }

    override fun didUserWantToSyncBy(user: GROUP) {
        syncManager.sync(user)
    }

    private fun setProgressSyncItem(progress: Progress) {
        when {
            isProgressZero(progress) ->
                view.setSyncItem(false,
                    view.getStringWithParams(R.string.syncing_calculating),
                    R.drawable.ic_syncing)
            else -> {
                val messageRes = if (progress is UploadProgress) {
                    R.string.sync_progress_upload_notification_content
                } else {
                    R.string.sync_progress_download_notification_content
                }

                view.setSyncItem(false,
                    view.getStringWithParams(messageRes, progress.currentValue, progress.maxValue),
                    R.drawable.ic_syncing)
            }
        }
    }

    private fun isProgressZero(progress: Progress): Boolean {
        return progress.currentValue == 0 && progress.maxValue == 0
    }

    private fun setCompleteSyncItem() {
        setSyncItem(true, R.string.nav_sync_complete, R.drawable.ic_sync_success)
    }

    private fun setSyncItem(enabled: Boolean, @StringRes title: Int, @DrawableRes icon: Int) {
        view.setSyncItem(enabled, view.getStringWithParams(title), icon)
    }

    private fun setErrorSyncItem() {
        setSyncItem(true, R.string.nav_sync_failed, R.drawable.ic_sync_failed)
    }
}
