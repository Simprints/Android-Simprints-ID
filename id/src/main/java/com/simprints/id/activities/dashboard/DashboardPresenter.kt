package com.simprints.id.activities.dashboard

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.simprints.id.R
import com.simprints.id.data.DataManager
import com.simprints.id.exceptions.unsafe.InvalidSyncGroupError
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.services.sync.SyncClient
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.libcommon.Progress
import com.simprints.libdata.AuthListener
import com.simprints.libdata.ConnectionListener
import com.simprints.libdata.tools.Constants.GROUP
import io.reactivex.observers.DisposableObserver
import timber.log.Timber

class DashboardPresenter(val view: DashboardContract.View,
                         val syncClient: SyncClient,
                         val dataManager: DataManager) : DashboardContract.Presenter {

    private var started: Boolean = false

    init {
        view.setPresenter(this)
    }

    override fun start() {
        if (!started) {
            started = true
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
            dataManager.unregisterRemoteAuthListener(authListener)
            dataManager.unregisterRemoteConnectionListener(connectionListener)
        } catch (error: UninitializedDataManagerError) {
            handleUnexpectedError(error)
        }
    }

    private fun startListeners() {
        dataManager.registerRemoteAuthListener(authListener)
        dataManager.registerRemoteConnectionListener(connectionListener)
        updateConnectionState()
        syncClient.startListening(newSyncObserver())
    }

    private fun updateConnectionState() {
        if (dataManager.isRemoteConnected) {
            connectionListener.onConnection()
        } else {
            connectionListener.onDisconnection()
        }
    }

    private val authListener = object : AuthListener {
        override fun onSignIn() {
        }

        override fun onSignOut() {
            setOfflineSyncItem()
        }
    }

    private val connectionListener = object : ConnectionListener {
        override fun onConnection() {
            setReadySyncItem()
        }

        override fun onDisconnection() {
            setOfflineSyncItem()
        }
    }

    private fun newSyncObserver(): DisposableObserver<Progress> {
        return object : DisposableObserver<Progress>() {

            override fun onNext(progress: Progress) {
                Timber.d("onNext")
                setProgressSyncItem(progress)
            }

            override fun onComplete() {
                Timber.d("onComplete")
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

    private fun setReadySyncItem() {
        setSyncItem(true, R.string.nav_sync, R.drawable.ic_menu_sync_ready)
    }

    private fun setOfflineSyncItem() {
        setSyncItem(false, R.string.not_signed_in, R.drawable.ic_menu_sync_off)
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
