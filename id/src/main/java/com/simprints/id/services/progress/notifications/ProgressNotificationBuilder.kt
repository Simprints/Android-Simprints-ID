package com.simprints.id.services.progress.notifications

import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.simprints.id.services.progress.Progress
import io.reactivex.observers.DisposableObserver


class ProgressNotificationBuilder(notificationManager: NotificationManager,
                                  notificationBuilder: NotificationCompat.Builder,
                                  tag: String,
                                  title: String,
                                  icon: Int,
                                  progressTextBuilder: (progress: Progress) -> String)
    : BaseNotificationBuilder(notificationManager, notificationBuilder, tag, title, icon) {

    init {
        updateBuilder {
            cancelAllNotifications()
            setOngoing(true)
            setProgress(0, 0, true)
            setContentText(progressTextBuilder(Progress(0, 0)))
        }
    }

    override val progressObserver: DisposableObserver<Progress> =
            object : DisposableObserver<Progress>() {
                override fun onComplete() {
                    finish()
                }

                override fun onError(e: Throwable) {
                    finish()
                }

                override fun onNext(progress: Progress) {
                    updateBuilder {
                        setProgress(progress.maxValue, progress.currentValue, progress.maxValue == 0)
                        setContentText(progressTextBuilder(progress))
                    }
                    notifyIfVisible()
                }
            }

    private fun finish() {
        updateBuilder {
            setProgress(0, 0, false)
            setOngoing(false)
        }
        cancelIfVisible()
        progressObserver.dispose()
    }

}
