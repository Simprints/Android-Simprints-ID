package com.simprints.infra.camera.helpers

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Coordinates when camera frames may be emitted to consumers.
 *
 * This helper keeps track of two concerns:
 * 1) **Caller readiness** - consumers can temporarily pause frame delivery while they are busy processing the previous frame.
 * 2) **Capture mode** - in regular mode the frames are emitted directly;
 *       in high-resolution mode analyzer is used as triggers for a manual still capture.
 *
 * ### State model
 * - [isFrameEmissionEnabled] controls whether any new frame should be delivered to downstream collectors.
 * - [isHighResolutionEnabled] switches behavior between direct analyzer emission and manual still capture.
 * - [isHighResolutionCaptureInProgress] prevents overlapping still captures in high-resolution mode.
 *
 * ### Typical high-resolution flow
 * 1. Analyzer callback asks [beginHighResolutionCapture].
 * 2. If it returns `true`, caller starts one manual image capture.
 * 3. On success, caller invokes [completeHighResolutionCapture] to clear in-flight state and check whether
 *      the resulting frame should still be emitted.
 * 4. On failure/cancellation, caller invokes [cancelHighResolutionCapture] to clear in-flight state.
 */
internal class FrameEmissionHelper {
    private val isFrameEmissionEnabled = AtomicBoolean(true)
    private val isHighResolutionEnabled = AtomicBoolean(false)
    private val isHighResolutionCaptureInProgress = AtomicBoolean(false)

    fun configure(highResolution: Boolean) {
        isHighResolutionEnabled.set(highResolution)
        isHighResolutionCaptureInProgress.set(false)
    }

    fun reset() {
        isFrameEmissionEnabled.set(true)
        isHighResolutionEnabled.set(false)
        isHighResolutionCaptureInProgress.set(false)
    }

    fun setFrameEmissionEnabled(enabled: Boolean) {
        isFrameEmissionEnabled.set(enabled)
    }

    /**
     * Returns `true` only when direct analyzer frames should be emitted.
     */
    fun shouldEmitAnalyserFrame(): Boolean = isFrameEmissionEnabled.get() && !isHighResolutionEnabled.get()

    /**
     * Returns `true` when:
     * - emission is currently enabled,
     * - high-resolution mode is enabled,
     * - there is no other capture already in progress.
     */
    fun beginHighResolutionCapture(): Boolean = isFrameEmissionEnabled.get() &&
        isHighResolutionEnabled.get() &&
        isHighResolutionCaptureInProgress.compareAndSet(false, true)

    /**
     * Marks the current high-resolution capture as complete and returns whether the captured frame should still be emitted.
     * Emission can be disabled while capture is in flight; in that case this returns `false`.
     */
    fun completeHighResolutionCapture(): Boolean {
        isHighResolutionCaptureInProgress.set(false)
        return isFrameEmissionEnabled.get()
    }

    fun cancelHighResolutionCapture() {
        isHighResolutionCaptureInProgress.set(false)
    }
}
