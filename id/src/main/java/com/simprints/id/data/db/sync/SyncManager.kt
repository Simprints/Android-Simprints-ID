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

    private var internalSyncObserver: DisposableObserver<Progress> = createInternalDisposable()

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
            internalSyncObserver.dispose()
        } catch (error: UninitializedDataManagerError) {
            handleUnexpectedError(error)
        }
    }

    private fun startListeners() {
        stopListeners()

        internalSyncObserver.dispose()
        internalSyncObserver = createInternalDisposable()
        syncClient.startListening(internalSyncObserver)
    }


    private fun createInternalDisposable(): DisposableObserver<Progress> =
        object : DisposableObserver<Progress>() {

            override fun onNext(progress: Progress) {
                Timber.d("onSyncProgress")
                uiObserver?.onNext(progress)
            }

            override fun onComplete() {
                Timber.d("onComplete")
                syncClient.stopListening()
                syncClient.stop()

                uiObserver?.onComplete()
            }

            override fun onError(throwable: Throwable) {
                Timber.d("onError")
                dataManager.logThrowable(throwable)
                syncClient.stopListening()
                syncClient.stop()

                uiObserver?.onError(throwable)
            }
        }

    private fun handleUnexpectedError(error: Error) {
        dataManager.logThrowable(error)
    }
}
