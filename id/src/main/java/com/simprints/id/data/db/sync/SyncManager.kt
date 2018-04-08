package com.simprints.id.data.db.sync

import com.simprints.id.data.DataManager
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.sync.SyncClient
import com.simprints.id.services.sync.SyncTaskParameters
import io.reactivex.observers.DisposableObserver
import timber.log.Timber

class SyncManager(private val dataManager: DataManager,
                  private val syncClient: SyncClient) {

    private var internalSyncObserver: DisposableObserver<Progress> = createInternalDisposable()

    // hashset to avoid duplicates
    var observers = hashSetOf<DisposableObserver<Progress>>()

    fun sync(syncParams: SyncTaskParameters) {
        syncClient.sync(syncParams, {
            startListeners()
        }, {
            observers.forEach { it.onError(Throwable("Server busy")) }
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

    // When a DisposeObserver is disposed, can not be reused.
    // So we create every time an internal one and forward the emits to
    // the other observers
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

                observers.forEach { it.onNext(progress) }
            }

            override fun onComplete() {
                Timber.d("onComplete")
                syncClient.stopListening()
                syncClient.stop()

                observers.forEach { it.onComplete() }
            }

            override fun onError(throwable: Throwable) {
                Timber.d("onError")
                dataManager.logThrowable(throwable)
                syncClient.stopListening()
                syncClient.stop()

                observers.forEach { it.onError(throwable) }
            }
        }

    private fun handleUnexpectedError(error: Error) {
        dataManager.logThrowable(error)
    }

    fun remoteObservers() {
        observers.clear()
    }

    fun addObserver(syncObserver: DisposableObserver<Progress>) {
        observers.add(syncObserver)
    }
}
