package com.simprints.id.services.progress.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.services.progress.Progress
import com.simprints.id.tools.InternalConstants
import io.reactivex.observers.DisposableObserver


class ErrorNotificationBuilder(notificationManager: NotificationManager,
                               notificationBuilder: NotificationCompat.Builder,
                               tag: String,
                               title: String,
                               icon: Int,
                               private val errorTextBuilder: (throwable: Throwable) -> String)
    : BaseNotificationBuilder(notificationManager, notificationBuilder, tag, title, icon) {

    override val progressObserver: DisposableObserver<Progress> =
        object : DisposableObserver<Progress>() {
            override fun onComplete() {
                dispose()
            }

            override fun onError(throwable: Throwable) {
                setToLaunchDashboardActivity()
                showErrorNotification(throwable)
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

    private fun setToLaunchDashboardActivity() {
        notificationBuilder.setContentIntent(getIntentForDashboardActivity())
        notificationBuilder.setAutoCancel(true)
    }

    private fun getIntentForDashboardActivity() =
        PendingIntent.getActivity(notificationBuilder.mContext,
            InternalConstants.LAST_GLOBAL_REQUEST_CODE,
            Intent(notificationBuilder.mContext, DashboardActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT)

}
