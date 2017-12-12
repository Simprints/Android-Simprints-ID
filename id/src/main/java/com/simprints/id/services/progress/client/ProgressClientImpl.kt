package com.simprints.id.services.progress.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.simprints.id.services.progress.service.ProgressTaskParameters
import com.simprints.id.throwables.unsafe.InvalidServiceError
import timber.log.Timber


open class ProgressClientImpl<in T: ProgressTaskParameters> (private val context: Context,
                                                             private val serviceClass: Class<*>)
    : ProgressClient<T> {

    private val intent = Intent(context, serviceClass)

    init {
        context.packageManager.resolveService(intent, 0)
                ?: throw InvalidServiceError.forService(serviceClass)
    }

    override fun startService() {
        Timber.d("startService()")
        context.startService(intent)
                ?: throw InvalidServiceError.forService(serviceClass)
    }

    override fun startForegroundService() {
        Timber.d("startForegroundService()")
        context.startForegroundServiceCompat(intent)
                ?: throw InvalidServiceError.forService(serviceClass)
    }

    /**
     * Note: We are reimplementing ContextCompact.startForegroundService here because it
     * does not pass the return value of context.startForegroundService.
     */
    private fun Context.startForegroundServiceCompat(intent: Intent): ComponentName? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.startForegroundService(intent)
            } else {
                // Pre-O behavior.
                this.startService(intent)
            }

    override fun bind(): ProgressConnection<T> {
        Timber.d("bind()")
        return ProgressConnectionImpl.open(context, intent)
    }

    override fun stop() {
        Timber.d("stopService()")
        context.stopService(intent)
    }

}
