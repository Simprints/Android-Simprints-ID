package com.simprints.id.activities.dashboard

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.simprints.id.R
import com.simprints.id.data.DataManager
import com.simprints.id.exceptions.unsafe.InvalidSyncGroupError
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError
import com.simprints.id.libdata.models.realm.rl_Person
import com.simprints.id.libdata.tools.Constants
import com.simprints.id.libdata.tools.Constants.GROUP
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.services.sync.SyncClient
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.libcommon.Progress
import io.reactivex.observers.DisposableObserver
import timber.log.Timber

class DashboardPresenter(val view: DashboardContract.View,
                         val syncClient: SyncClient,
                         val dataManager: DataManager) : DashboardContract.Presenter {

    private var started: Boolean = false

    override fun start() {
        if (!started) {
            started = true

            dataManager.syncGroup = Constants.GROUP.GLOBAL
            val realm = dataManager.getRealmInstance()
            realm.executeTransaction {
                it.where(rl_Person::class.java).findAll().deleteAllFromRealm()
            }

            startListeners()
        }
    }

    override fun pause() {
        stopListeners()
    }

    override fun sync() {
        val syncParameters = when (dataManager.syncGroup) {
            GROUP.GLOBAL -> SyncTaskParameters.GlobalSyncTaskParameters(dataManager.getSignedInProjectIdOrEmpty())
            GROUP.USER -> SyncTaskParameters.UserSyncTaskParameters(dataManager.getSignedInProjectIdOrEmpty(), dataManager.getSignedInUserIdOrEmpty())
            else -> {
                handleUnexpectedError(InvalidSyncGroupError())
                return
            }
        }

        syncClient.sync(syncParameters, {
            syncClient.startListening(newSyncObserver())
        }, {
            setErrorSyncItem()
            view.showToast(R.string.wait_for_current_sync_to_finish)
        })
    }

    private fun stopListeners() {
        try {
            syncClient.stopListening()
        } catch (error: UninitializedDataManagerError) {
            handleUnexpectedError(error)
        }
    }

    private fun startListeners() {
        syncClient.startListening(newSyncObserver())
    }

    private fun newSyncObserver(): DisposableObserver<Progress> {
        val start = System.currentTimeMillis()
        return object : DisposableObserver<Progress>() {

            override fun onNext(progress: Progress) {
                Timber.d("onNext")
                setProgressSyncItem(progress)
            }

            override fun onComplete() {
                Timber.d("onComplete")

                val ms = System.currentTimeMillis() - start
                val realm = dataManager.getRealmInstance()
                val count = realm.where(rl_Person::class.java).findAll().count()
                Timber.d("Syncer - $count Persons loaded in $ms ms (${ms / 1000})")

                setCompleteSyncItem()
                syncClient.stopListening()
            }

            override fun onError(throwable: Throwable) {
                Timber.d("onError")
                setErrorSyncItem()
                logThrowable(throwable)
                syncClient.stopListening()
            }

            private fun logThrowable(throwable: Throwable) {
                if (throwable is Error) {
                    dataManager.logError(throwable)
                } else if (throwable is RuntimeException) {
                    dataManager.logSafeException(throwable)
                }
            }
        }
    }

    private fun handleUnexpectedError(error: Error) {
        dataManager.logError(error)
        view.launchAlertView(ALERT_TYPE.UNEXPECTED_ERROR)
    }

    private fun setProgressSyncItem(progress: Progress) {
        if (isProgressZero(progress))
            view.setSyncItem(false,
                view.getStringWithParams(R.string.syncing_calculating),
                R.drawable.ic_syncing)
        else view.setSyncItem(false,
                view.getStringWithParams(R.string.syncing_with_progress, progress.currentValue, progress.maxValue),
                R.drawable.ic_syncing)
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

    private fun isProgressZero(progress: Progress): Boolean {
        return progress.currentValue == 0 && progress.maxValue == 0
    }
}
