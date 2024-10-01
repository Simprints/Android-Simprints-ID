package com.simprints.fingerprint.infra.scanner.capture

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FingerprintScanningStatusTracker @Inject constructor() {
    private val _scanCompleted = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val scanCompleted: SharedFlow<Unit> get() = _scanCompleted

    fun notifyScanCompleted() {
        _scanCompleted.tryEmit(Unit)
    }
}
