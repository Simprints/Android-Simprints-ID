package com.simprints.core.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppForegroundStateTracker @Inject constructor() {
    fun observeAppInForeground(): Flow<Boolean> = callbackFlow {
        val lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                trySend(true)
            }

            override fun onPause(owner: LifecycleOwner) {
                trySend(false)
            }
        }
        val lifecycle = ProcessLifecycleOwner.Companion.get().lifecycle
        lifecycle.addObserver(lifecycleObserver)
        awaitClose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
}
