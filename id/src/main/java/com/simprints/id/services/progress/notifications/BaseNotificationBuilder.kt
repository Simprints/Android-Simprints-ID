package com.simprints.id.services.progress.notifications

import android.app.Notification
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import java.util.concurrent.atomic.AtomicBoolean


abstract class BaseNotificationBuilder(private val notificationManager: NotificationManager,
                                       val notificationBuilder: NotificationCompat.Builder,
                                       override val tag: String,
                                       title: String,
                                       icon: Int)
    : NotificationBuilder {

    override val id = NotificationBuilder.newId()

    protected val visible = AtomicBoolean(false)

    private val builder: NotificationCompat.Builder =
            notificationBuilder
                    .setVibrate(notificationVibrationPattern)
                    .setOnlyAlertOnce(true)
                    .setContentTitle(title)
                    .setSmallIcon(icon)

    protected fun updateBuilder(op: NotificationCompat.Builder.() -> Unit) =
            synchronized(builder) {
                builder.op()
            }

    override fun build(): Notification {
        synchronized(builder) {
            return builder.build()
        }
    }

    override fun setVisibility(visible: Boolean) {
        this.visible.set(visible)
    }

    protected fun notifyIfVisible() {
        if (visible.get()) {
            notificationManager.notify(id, build())
        }
    }

    protected fun cancelIfVisible() {
        if (visible.get()) {
            notificationManager.cancel(id)
        }
    }

    protected fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    companion object {
        private val notificationVibrationPattern = longArrayOf(50, 75)
    }
}
