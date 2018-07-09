package com.simprints.id.services.progress.notifications

import android.app.NotificationManager
import android.support.v4.app.NotificationCompat
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.sync.SyncCategory
import io.reactivex.observers.DisposableObserver

class ErrorNotificationBuilder(notificationManager: NotificationManager,
                               notificationBuilder: NotificationCompat.Builder,
                               tag: String,
                               title: String,
                               icon: Int,
                               syncCategory: SyncCategory?,
                               private val errorTextBuilder: (throwable: Throwable) -> String)
    : BaseNotificationBuilder(notificationManager, notificationBuilder, tag, title, icon) {

    override val progressObserver: DisposableObserver<Progress> =
        object : DisposableObserver<Progress>() {
            override fun onComplete() {
                dispose()
            }

            override fun onError(throwable: Throwable) =
                when (syncCategory) {
                    SyncCategory.SCHEDULED_BACKGROUND,
                    SyncCategory.AT_LAUNCH -> doNotShowNotification()
                    else -> showErrorNotification(throwable)
                }

            private fun doNotShowNotification() {
                dispose()
            }

            private fun showErrorNotification(throwable: Throwable) {
                updateBuilder {
                    setContentText(errorTextBuilder(throwable))
                }
                notifyIfVisible()
                dispose()
            }

            override fun onNext(progress: Progress) {
                cancelIfVisible()
            }
        }
}
