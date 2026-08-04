package com.simprints.infra.camera

import android.graphics.Bitmap
import androidx.lifecycle.LifecycleOwner
import com.simprints.infra.logging.LoggingConstants
import kotlinx.coroutines.flow.Flow

interface CameraFrameProvider {

    /**
     * Cold flow of continuous low-resolution frames. Starts producing when the first collector
     * subscribes. Stops when [release] is called or the lifecycle ends.
     * Bitmaps are already rotation-corrected (always upright).
     */
    val frames: Flow<Bitmap>

    /**
     * Prepares the camera pipeline. Must be called before collecting [frames] or calling
     * [takePicture]. Safe to call from any thread.
     */
    suspend fun bind(
        lifecycleOwner: LifecycleOwner,
        surface: CameraPreviewView,
    )

    /**
     * One-shot high-resolution capture. Returns null if capture fails.
     */
    suspend fun takePicture(): Bitmap?

    /**
     * Enables or disables the camera torch.
     */
    fun setTorchEnabled(enabled: Boolean)

    /**
     * Releases the camera pipeline. After this call [frames] will complete.
     */
    fun release()

    interface Factory {
        fun create(crashReportTag: LoggingConstants.CrashReportTag): CameraFrameProvider
    }
}
