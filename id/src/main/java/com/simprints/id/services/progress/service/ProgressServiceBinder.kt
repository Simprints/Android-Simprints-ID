package com.simprints.id.services.progress.service

import com.simprints.libcommon.Progress
import io.reactivex.Observable


interface ProgressServiceBinder<in T: ProgressTaskParameters> {

    /**
     * Source of progress updates that starts by replaying all the event emitted by this service.
     */
    val progressReplayObservable: Observable<Progress>

    /**
     * Request the service to execute its task with the specified taskParameters
     * Throw TaskInProgressException if it is already executing a task with different taskParameters
     */
    fun execute(taskParameters: T)

    /**
     * Makes the service run in the foreground, and shows the user the service's progress
     * progressNotificationBuilder while in this state.
     */
    fun startForeground()

    /**
     * If the service is running in the foreground, moves it to the background
     * (allowing it to be killed if more memory is needed) and removes the corresponding
     * progressNotificationBuilder.
     */
    fun stopForeground()

}
