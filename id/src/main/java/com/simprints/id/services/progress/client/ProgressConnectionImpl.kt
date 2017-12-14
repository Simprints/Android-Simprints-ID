package com.simprints.id.services.progress.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.simprints.id.services.progress.service.ProgressServiceBinder
import com.simprints.id.services.progress.service.ProgressTaskParameters
import timber.log.Timber
import java.util.concurrent.Semaphore


class ProgressConnectionImpl<in T: ProgressTaskParameters>(private val context: Context,
                                                           private val serviceConnection: ServiceConnection,
                                                           private val serviceBinder: ProgressServiceBinder<T>)
    : ProgressConnection<T>,
        ProgressServiceBinder<T> by serviceBinder{

    companion object {

        fun <T: ProgressTaskParameters> open(context: Context, serviceIntent: Intent): ProgressConnection<T> {
            lateinit var serviceBinder: ProgressServiceBinder<T>
            val serviceConnection = context.bindServiceSync(
                    serviceIntent,
                    getBinderOnConnection<T> { binder -> serviceBinder = binder },
                    throwOnDisconnection()
            )
            return ProgressConnectionImpl(context, serviceConnection, serviceBinder)
        }

        @Suppress("UNCHECKED_CAST")
        private fun <T: ProgressTaskParameters> getBinderOnConnection(init: (binder: ProgressServiceBinder<T>) -> Unit) =
                { name: ComponentName?, binder: IBinder? ->
                    Timber.d("onServiceConnected($name, $binder)")
                    init((binder as ProgressServiceBinder<T>))
                }

        private fun throwOnDisconnection() =
                { name: ComponentName? ->
                    Timber.d("onServiceDisconnected($name)")
                    throw Exception("This should not happen")
                }


        private fun Context.bindServiceSync(serviceIntent: Intent,
                                            onConnection: (name: ComponentName?, binder: IBinder?) -> Unit,
                                            onDisconnection: (name: ComponentName?) -> Unit)
                : ServiceConnection {
            val connectionLock = Semaphore(0)
            val serviceConnection = buildServiceConnection(
                    doOnConnectionThenReleaseLock(onConnection, connectionLock),
                    onDisconnection
            )
            this.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            connectionLock.acquire()
            return serviceConnection
        }

        private fun doOnConnectionThenReleaseLock(onConnection: (name: ComponentName?, binder: IBinder?) -> Unit,
                                                  lock: Semaphore) =
                { name: ComponentName?, binder: IBinder? ->
                    onConnection(name, binder)
                    lock.release()
                }

        private fun buildServiceConnection(onConnection: (name: ComponentName?, binder: IBinder?) -> Unit,
                                           onDisconnection: (name: ComponentName?) -> Unit) =
                object : ServiceConnection {

                    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                        onConnection(name, binder)
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                        onDisconnection(name)
                    }
                }

    }

    override fun unbind() {
        Timber.d("unbind()")
        context.unbindService(serviceConnection)
    }
}
