package com.simprints.id.data.db.sync

import com.simprints.id.data.DataManager
import com.simprints.id.data.db.sync.model.SyncManagerState
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.sync.SyncClient
import com.simprints.id.services.sync.SyncTaskParameters
import io.reactivex.observers.DisposableObserver
import timber.log.Timber

class SyncManager(private val dataManager: DataManager,
                  private val syncClient: SyncClient) {

    var state: SyncManagerState = SyncManagerState.NOT_STARTED

    private var internalSyncObserver: DisposableObserver<Progress> = createInternalDisposable()

    // hashset to avoid duplicates
    private var observers = hashSetOf<DisposableObserver<Progress>>()

    fun sync(syncParams: SyncTaskParameters) {
        SyncManagerState.STARTED
        syncClient.sync(syncParams, {
            startListeners()
        }, {
            state = SyncManagerState.FAILED
            observers.forEach { it.onError(Throwable("Server busy")) }
            stopListeners()
        })
    }

    fun stop() {
        //syncClient.stopSync()
        stopListeners()
    }

    fun stopListeners() {
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
                state = SyncManagerState.IN_PROGRESS
                Timber.d("onSyncProgress")

                // Some callback can call SyncManager Api and modify "observers". That can cause
                // an exception because "observers" is still in a loop
                val observersToNotify = observers.toMutableSet()
                observersToNotify.forEach { it.onNext(progress) }
            }

            override fun onComplete() {
                state = SyncManagerState.SUCCEED

                Timber.d("onComplete")
                syncClient.stopListening()
                syncClient.stop()

                // See onNext
                val observersToNotify = observers.toMutableSet()
                observersToNotify.forEach { it.onComplete() }
            }

            override fun onError(throwable: Throwable) {
                state = SyncManagerState.FAILED

                Timber.d("onError")
                dataManager.logThrowable(throwable)
                syncClient.stopListening()
                syncClient.stop()

                // See onNext
                val observersToNotify = observers.toMutableSet()
                observersToNotify.forEach { it.onError(throwable) }
            }
        }

    private fun handleUnexpectedError(error: Error) {
        dataManager.logThrowable(error)
    }

    fun removeObservers() {
        observers.clear()
    }

    fun addObserver(syncObserver: DisposableObserver<Progress>) {
        observers.add(syncObserver)
    }
}
