package com.simprints.id.services.progress.notifications

import android.app.Notification
import com.simprints.libcommon.Progress
import io.reactivex.observers.DisposableObserver
import java.util.concurrent.atomic.AtomicInteger


interface NotificationBuilder {

    companion object {

        private val lastId = AtomicInteger(0)

        fun newId(): Int =
                lastId.addAndGet(1)

    }

    val id: Int
    val tag: String
    val progressObserver: DisposableObserver<Progress>

    fun build(): Notification

    fun setVisibility(visible: Boolean)

}
