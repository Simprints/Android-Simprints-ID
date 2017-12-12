package com.simprints.id.services.sync

import android.content.Context
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.progress.client.ProgressClientImpl
import com.simprints.id.throwables.safe.TaskInProgressException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg


class SyncClient(context: Context)
    : ProgressClientImpl<SyncTaskParameters>(context, SyncService::class.java) {

    private var disposable: Disposable? = null

    fun sync(syncParameters: SyncTaskParameters,
             observer: DisposableObserver<Progress>,
             uiUpdateOnBusy: () -> Unit) {
        async(UI) {
            val started = bg {
                startService()
                val connection = bind()
                val started = try {
                    connection.execute(syncParameters)
                    connection.startForeground()
                    synchronized(this) {
                        disposable = connection.progressReplayObservable
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeWith(observer)
                    }
                    true
                } catch (exception: TaskInProgressException) {
                    false
                }
                connection.unbind()
                started
            }.await()
            if (!started) {
                uiUpdateOnBusy()
            }
        }
    }

    fun stopListening() {
        synchronized(this) {
            disposable?.dispose()
        }
    }

}