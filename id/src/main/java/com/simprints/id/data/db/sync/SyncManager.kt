package com.simprints.id.data.db.sync

import com.simprints.id.data.DataManager
import com.simprints.id.domain.Constants
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.sync.SyncClient
import com.simprints.id.services.sync.SyncTaskParameters.*
import io.reactivex.observers.DisposableObserver
import timber.log.Timber

class SyncManager(private val dataManager: DataManager,
                  private val syncClient: SyncClient,
                  private val uiObserver: DisposableObserver<Progress>? = null) {

    fun sync(user: Constants.GROUP) {

        dataManager.syncGroup = user
        val syncParameters = when (user) {
            Constants.GROUP.GLOBAL -> GlobalSyncTaskParameters(dataManager.getSignedInProjectIdOrEmpty())
            Constants.GROUP.USER -> UserSyncTaskParameters(dataManager.getSignedInProjectIdOrEmpty(), dataManager.getSignedInUserIdOrEmpty())
            Constants.GROUP.MODULE -> ModuleIdSyncTaskParameters(dataManager.getSignedInProjectIdOrEmpty(), dataManager.moduleId)
        }

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
        syncClient.startListening(internalSyncObserver)
        uiObserver?.let { syncClient.startListening(uiObserver) }
    }

    private val internalSyncObserver: DisposableObserver<Progress> = object : DisposableObserver<Progress>() {

        override fun onNext(progress: Progress) {
            Timber.d("onNext")
        }

        override fun onComplete() {
            Timber.d("onComplete")
            syncClient.stopListening()
        }

        override fun onError(throwable: Throwable) {
            Timber.d("onError")
            dataManager.logThrowable(throwable)
            syncClient.stopListening()
        }
    }

    private fun handleUnexpectedError(error: Error) {
        dataManager.logThrowable(error)
    }
}
