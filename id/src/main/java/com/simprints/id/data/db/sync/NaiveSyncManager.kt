package com.simprints.id.data.db.sync

import com.simprints.id.data.DataManager
import com.simprints.id.domain.Constants
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.sync.SyncClient
import com.simprints.id.services.sync.SyncTaskParameters
import io.reactivex.observers.DisposableObserver
import timber.log.Timber

class NaiveSyncManager(private val dataManager: DataManager,
                       private val syncClient: SyncClient,
                       private val uiObserver: DisposableObserver<Progress>? = null) {

    fun sync(group: Constants.GROUP) {

        dataManager.syncGroup = group
        val syncParameters = SyncTaskParameters.build(group, dataManager)

        syncClient.sync(syncParameters, {
            startListeners()
        }, {
            uiObserver?.onError(Throwable("Server busy"))
            stopListeners()
        })
    }

    fun stop() {
        stopListeners()
    }

    private fun stopListeners() {
        try {
            syncClient.stopListening()
        } catch (error: UninitializedDataManagerError) {
            handleUnexpectedError(error)
        }
    }

    private fun startListeners() {
        stopListeners()
        uiObserver?.let { syncClient.startListening(uiObserver) }
        syncClient.startListening(internalSyncObserver)
    }

    private val internalSyncObserver: DisposableObserver<Progress> = object : DisposableObserver<Progress>() {

        override fun onNext(progress: Progress) {
            Timber.d("onNext")
        }

        override fun onComplete() {
            Timber.d("onComplete")
            stopListeners()
        }

        override fun onError(throwable: Throwable) {
            Timber.d("onError")
            stopListeners()
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

    private fun handleUnexpectedError(error: Error) {
        dataManager.logError(error)
    }
}
