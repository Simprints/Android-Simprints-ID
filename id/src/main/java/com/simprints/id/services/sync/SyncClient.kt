package com.simprints.id.services.sync

import android.content.Context
import com.simprints.id.exceptions.safe.TaskInProgressException
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.progress.client.ProgressClientImpl
import com.simprints.id.services.progress.client.ProgressConnection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import timber.log.Timber

class SyncClient(context: Context)
    : ProgressClientImpl<SyncTaskParameters>(context, SyncService::class.java) {

    private var currentProgressReplayObservable: Observable<Progress>? = null
    private val disposables = mutableListOf<Disposable>()

    fun sync(syncParameters: SyncTaskParameters,
             onStarted: () -> Unit,
             onBusy: (e: Exception) -> Unit) {
        async(UI) {
            try {
                bg { startSyncAndObserve(syncParameters) }.await()
                onStarted()
            } catch (exception: TaskInProgressException) {
                onBusy(exception)
            }
        }
    }

    private fun startSyncAndObserve(syncParameters: SyncTaskParameters) {
        Timber.d("startSyncAndObserve()")
        startService()
        connectAnd {
            execute(syncParameters)
            startForeground()
            setProgressReplayObservable(progressReplayObservable)
        }
    }

    private fun <T> connectAnd(op: ProgressConnection<SyncTaskParameters>.() -> T): T {
        val connection = bind()
        val result = connection.op()
        connection.unbind()
        return result
    }

    private fun setProgressReplayObservable(observable: Observable<Progress>) {
        currentProgressReplayObservable = observable
    }

    fun startListening(observer: DisposableObserver<Progress>) {
        Timber.d("startListening()")
        val observable = currentProgressReplayObservable
        if (observable != null) {
            disposables.add(observable
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribeWith(observer))
        }
    }

    fun stopListening() {
        Timber.d("stopListening()")
        synchronized(this) {
            disposables.forEach {
                it.dispose()
            }
            disposables.clear()
        }
    }
}
