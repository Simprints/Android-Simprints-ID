package com.simprints.id.services.progress.notifications

import android.app.NotificationManager
import android.support.v4.app.NotificationCompat
import com.simprints.id.services.progress.Progress
import io.reactivex.observers.DisposableObserver


class CompleteNotificationBuilder(private val notificationManager: NotificationManager,
                                  notificationBuilder: NotificationCompat.Builder,
                                  tag: String,
                                  title: String,
                                  icon: Int,
                                  private val completeTextBuilder: () -> String)
    : BaseNotificationBuilder(notificationBuilder, tag, title, icon) {

    override val progressObserver: DisposableObserver<Progress> =
            object : DisposableObserver<Progress>() {
                override fun onComplete() {
                    updateBuilder { setContentText(completeTextBuilder()) }
                    if (visible.get()) {
                        notificationManager.notify(id, build())
                    }
                    dispose()
                }

                override fun onError(throwable: Throwable) {
                    dispose()
                }

                override fun onNext(progress: Progress) { }
            }

}
