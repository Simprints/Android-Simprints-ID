package com.simprints.id.services.progress.notifications

import android.app.NotificationManager
import android.support.v4.app.NotificationCompat
import com.simprints.libcommon.Progress
import io.reactivex.observers.DisposableObserver
import timber.log.Timber


class ResultNotificationBuilder(private val notificationManager: NotificationManager,
                                notificationBuilder: NotificationCompat.Builder,
                                tag: String,
                                title: String,
                                icon: Int,
                                private val completeText: String,
                                private val errorTextBuilder: (throwable: Throwable) -> String)
    : BaseNotificationBuilder(notificationBuilder, tag, title, icon) {

    override val progressObserver: DisposableObserver<Progress> =
            object : DisposableObserver<Progress>() {
                override fun onComplete() {
                    finishWithText(completeText)
                }

                override fun onError(throwable: Throwable) {
                    finishWithText(errorTextBuilder(throwable))
                }

                override fun onNext(progress: Progress) {
                }
            }

    private fun finishWithText(text: String) {
        updateBuilder {
            setContentText(text)
        }
        Timber.d("finishWithText()")
        if (visible.get()) {
            Timber.d("notify(id=$id)")
            notificationManager.notify(id, build())
        }
        progressObserver.dispose()
    }

}
