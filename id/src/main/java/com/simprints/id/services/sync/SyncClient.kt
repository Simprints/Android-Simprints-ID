package com.simprints.id.services.sync

import android.content.Context
import com.simprints.id.exceptions.safe.TaskInProgressException
import com.simprints.id.services.progress.client.ProgressClientImpl
import com.simprints.libcommon.Progress
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import timber.log.Timber


class SyncClient(context: Context)
    : ProgressClientImpl<SyncTaskParameters>(context, SyncService::class.java) {

    private var progressReplayObservable: Observable<Progress>? = null
    private val disposables = mutableListOf<Disposable>()

    fun sync(syncParameters: SyncTaskParameters,
             onStarted: () -> Unit,
             onBusy: () -> Unit) {
        async(UI) {
            val started = bg {
                startService()
                val connection = bind()
                val started = try {
                    connection.execute(syncParameters)
                    connection.startForeground()
                    setProgressReplayObservable(connection.progressReplayObservable)
                    true
                } catch (exception: TaskInProgressException) {
                    false
                }
                connection.unbind()
                started
            }.await()
            if (started) {
                onStarted()
            } else {
                onBusy()
            }
        }
    }

    private fun setProgressReplayObservable(observable: Observable<Progress>) {
        synchronized(this) {
            progressReplayObservable = observable
        }
    }

    fun startListening(observer: DisposableObserver<Progress>) {
        Timber.d("startListening()")
        synchronized(this) {
            val observable = progressReplayObservable
            if (observable != null) {
                disposables.add(observable
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(observer))
            }
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