package com.simprints.id.services.progress.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.progress.notifications.ProgressNotificationBuilder
import com.simprints.id.services.progress.notifications.ResultNotificationBuilder
import com.simprints.id.exceptions.safe.TaskInProgressException
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.ReplaySubject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


@SuppressLint("Registered")
abstract class ProgressService<in T : ProgressTaskParameters> : Service() {


    private val isInterrupted: AtomicBoolean = AtomicBoolean(false)

    private val finishObserver = object : DisposableObserver<Progress>() {
        override fun onComplete() {
            Timber.d("onComplete")
            stopSelf()
            dispose()
        }

        override fun onError(throwable: Throwable) {
            Timber.d("onError($throwable)")
            stopSelf()
            dispose()
        }

        override fun onNext(progress: Progress) {
            Timber.d("onNext($progress)")
        }
    }

    private val executing = AtomicBoolean(false)
    private lateinit var task: ProgressTask
    private lateinit var taskParameters: T

    private lateinit var progressLiveObservable: Observable<Progress>
    private lateinit var progressReplayObservable: ReplaySubject<Progress>

    private lateinit var progressNotificationBuilder: ProgressNotificationBuilder
    private lateinit var resultNotificationBuilder: ResultNotificationBuilder

    override fun onCreate() {
        Timber.d("onCreate()")
        super.onCreate()
        progressReplayObservable = ReplaySubject.create<Progress>()
    }

    override fun onBind(intent: Intent?): IBinder? {
        Timber.d("onBind()")
        return ProgressServiceBinderImpl()
    }

    inner class ProgressServiceBinderImpl : android.os.Binder(), ProgressServiceBinder<T> {

        override val progressReplayObservable: Observable<Progress> =
                this@ProgressService.progressReplayObservable

        override fun execute(taskParameters: T) =
                this@ProgressService.execute(taskParameters)

        override fun startForeground() {
            startForeground(progressNotificationBuilder.id, progressNotificationBuilder.build())
            progressNotificationBuilder.setVisibility(true)
            resultNotificationBuilder.setVisibility(true)
        }

        override fun stopForeground() {
            stopForeground(true)
            progressNotificationBuilder.setVisibility(false)
            resultNotificationBuilder.setVisibility(false)
        }

    }

    private fun execute(taskParameters: T) {
        if (executing.getAndSet(true)) {
            checkTaskOverlap(taskParameters)
        } else {
            initiateTask(taskParameters)
        }
    }

    private fun checkTaskOverlap(taskParameters: T) {
        if (this.taskParameters != taskParameters) {
            throw TaskInProgressException()
        }
    }

    private fun initiateTask(taskParameters: T) {
        this.taskParameters = taskParameters
        task = getTask(taskParameters)
        progressNotificationBuilder = getProgressNotificationBuilder(taskParameters)
        resultNotificationBuilder = getResultNotificationBuilder(taskParameters)
        progressLiveObservable = observableTask()
        progressLiveObservable.subscribe(progressReplayObservable)
        progressReplayObservable.subscribe(progressNotificationBuilder.progressObserver)
        progressReplayObservable.subscribe(finishObserver)
        // This delay is necessary on devices running O. Dirty...
        // If stopSelf happens after notify, it deletes the notification.
        progressReplayObservable.delay(100, TimeUnit.MILLISECONDS)
                .subscribe(resultNotificationBuilder.progressObserver)

    }

    abstract fun getTask(taskParameters: T): ProgressTask

    abstract fun getProgressNotificationBuilder(taskParameters: T): ProgressNotificationBuilder

    abstract fun getResultNotificationBuilder(taskParameters: T): ResultNotificationBuilder

    private fun observableTask(): Observable<Progress> =
            Observable.create<Progress>({ emitter ->
                task.run({ isInterrupted.get() }, emitter)
            })
                    .subscribeOn(AndroidSchedulers.mainThread())

    override fun onDestroy() {
        Timber.d("ProgressService: onDestroy()")
        super.onDestroy()
        isInterrupted.set(true)
    }

}
