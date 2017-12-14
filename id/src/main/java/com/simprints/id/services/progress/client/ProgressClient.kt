package com.simprints.id.services.progress.client

import com.simprints.id.services.progress.service.ProgressTaskParameters


/**
 * Throw InvalidServiceError on initialization if the service cannot be resolved
 */
interface ProgressClient<in T: ProgressTaskParameters> {

    /**
     * Start the service in the background if it is not already running.
     *
     * Note: Starting with Android O / 8.0 / Binder level 26, an app can create a background service
     * only if it is in foreground. If your app is in background, use [startForegroundService].
     * Learn more here: https://developer.android.com/about/versions/oreo/background.html#broadcasts
     */
    fun startService()

    /**
     * Similar to [startService], but with an implicit promise that the Service's
     * [android.app.Service.startForeground] will be called within ~5 seconds.
     * Otherwise the system will automatically stop the service and declare the app ANR.
     */
    fun startForegroundService()

    /**
     * Bind to the service, creating it if needed.
     * This operation should be performed on a background thread.
     */
    fun bind(): ProgressConnection<T>

    /**
     * Stop the service if it is currently running.
     */
    fun stop()

}
