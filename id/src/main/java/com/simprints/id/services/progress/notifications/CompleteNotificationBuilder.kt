package com.simprints.id.services.progress.notifications

import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.simprints.id.services.progress.Progress
import io.reactivex.observers.DisposableObserver


class CompleteNotificationBuilder(notificationManager: NotificationManager,
                                  notificationBuilder: NotificationCompat.Builder,
                                  tag: String,
                                  title: String,
                                  icon: Int,
                                  private val completeTextBuilder: () -> String)
    : BaseNotificationBuilder(notificationManager, notificationBuilder, tag, title, icon) {

    override val progressObserver: DisposableObserver<Progress> =
            object : DisposableObserver<Progress>() {
                override fun onComplete() {
                    updateBuilder {
                        setContentText(completeTextBuilder())
                    }
                    notifyIfVisible()
                    dispose()
                }

                override fun onError(throwable: Throwable) {
                    dispose()
                }

                override fun onNext(progress: Progress) { }
            }

}
