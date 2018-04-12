package com.simprints.id.services.progress.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.simprints.id.exceptions.safe.TaskInProgressException
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.progress.notifications.NotificationBuilder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@SuppressLint("Registered")
abstract class ProgressService<in T : ProgressTaskParameters> : Service() {

    companion object {
        val isRunning: AtomicBoolean = AtomicBoolean(false)
    }

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
            Timber.d("onSyncProgress($progress)")
        }
    }

    private val executing = AtomicBoolean(false)
    private lateinit var task: ProgressTask
    private var taskParameters: T? = null

    private lateinit var progressLiveObservable: Observable<Progress>
    private lateinit var progressReplayObservable: ReplaySubject<Progress>

    private lateinit var progressNotificationBuilder: NotificationBuilder
    private lateinit var completeNotificationBuilder: NotificationBuilder
    private lateinit var errorNotificationBuilder: NotificationBuilder

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
            taskParameters = null
            startForeground(progressNotificationBuilder.id, progressNotificationBuilder.build())
            setNotificationVisibility(true)
        }

        override fun stopForeground() {
            stopForeground(true)
            setNotificationVisibility(false)
            isRunning.set(false)
        }

        private fun setNotificationVisibility(visible: Boolean) {
            progressNotificationBuilder.setVisibility(visible)
            completeNotificationBuilder.setVisibility(visible)
            errorNotificationBuilder.setVisibility(visible)
        }
    }

    private fun execute(taskParameters: T) {
        isRunning.set(true)

        if (executing.getAndSet(true)) {
            checkTaskOverlap(taskParameters)
        } else {
            startExecution(taskParameters)
        }
    }

    private fun checkTaskOverlap(taskParameters: T) {
        if (this.taskParameters != taskParameters) {
            throw TaskInProgressException()
        }
    }

    private fun startExecution(taskParameters: T) {
        initTask(taskParameters)
        subscribeCoreObservers()
        initNotificationBuilders()
        subscribeNotificationProgressObservers()
    }

    private fun initTask(taskParameters: T) {
        this.taskParameters = taskParameters
        task = getTask(taskParameters)
        progressLiveObservable = task.run({ isInterrupted.get() })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
    }

    private fun subscribeCoreObservers() {
        progressReplayObservable.subscribe(finishObserver)
        progressLiveObservable.subscribe(progressReplayObservable)
    }

    private fun initNotificationBuilders() {
        val taskParameters = this.taskParameters?.let{
            progressNotificationBuilder = getProgressNotificationBuilder(it)
            completeNotificationBuilder = getCompleteNotificationBuilder(it)
            errorNotificationBuilder = getErrorNotificationBuilder(it)
        }
    }

    private fun subscribeNotificationProgressObservers() {
        progressReplayObservable.subscribe(progressNotificationBuilder.progressObserver)
        // Dirty... but necessary on devices running O, where any notification posted before
        // stopSelf() is cancelled on stopSelf()
        val delayedProgressReplayObservable = progressReplayObservable.delay(100, TimeUnit.MILLISECONDS)
        delayedProgressReplayObservable.subscribe(completeNotificationBuilder.progressObserver)
        delayedProgressReplayObservable.subscribe(errorNotificationBuilder.progressObserver)
    }

    abstract fun getTask(taskParameters: T): ProgressTask

    abstract fun getProgressNotificationBuilder(taskParameters: T): NotificationBuilder

    abstract fun getCompleteNotificationBuilder(taskParameters: T): NotificationBuilder

    abstract fun getErrorNotificationBuilder(taskParameters: T): NotificationBuilder

    override fun onDestroy() {
        Timber.d("ProgressService: onDestroy()")
        super.onDestroy()
        isInterrupted.set(true)
        isRunning.set(false)
    }
}
