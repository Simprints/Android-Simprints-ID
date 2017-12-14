package com.simprints.id.services.progress.notifications

import android.app.NotificationManager
import android.support.v4.app.NotificationCompat
import com.simprints.libcommon.Progress
import io.reactivex.observers.DisposableObserver


class ErrorNotificationBuilder(private val notificationManager: NotificationManager,
                               notificationBuilder: NotificationCompat.Builder,
                               tag: String,
                               title: String,
                               icon: Int,
                               private val errorTextBuilder: (throwable: Throwable) -> String)
    : BaseNotificationBuilder(notificationBuilder, tag, title, icon) {

    override val progressObserver: DisposableObserver<Progress> =
            object : DisposableObserver<Progress>() {
                override fun onComplete() {
                    dispose()
                }

                override fun onError(throwable: Throwable) {
                    updateBuilder { setContentText(errorTextBuilder(throwable)) }
                    if (visible.get()) {
                        notificationManager.notify(id, build())
                    }
                    dispose()
                }

                override fun onNext(progress: Progress) { }
            }

}
