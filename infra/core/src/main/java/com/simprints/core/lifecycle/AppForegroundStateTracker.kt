package com.simprints.core.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.simprints.core.DispatcherMain
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppForegroundStateTracker @Inject constructor(
    @param:DispatcherMain private val mainDispatcher: CoroutineDispatcher,
) {
    fun observeAppInForeground(): Flow<Boolean> = callbackFlow {
        val lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                trySend(true)
            }

            override fun onPause(owner: LifecycleOwner) {
                trySend(false)
            }
        }
        val lifecycle = ProcessLifecycleOwner.get().lifecycle
        lifecycle.addObserver(lifecycleObserver)
        awaitClose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }.flowOn(mainDispatcher) // runs in main thread by design
}
