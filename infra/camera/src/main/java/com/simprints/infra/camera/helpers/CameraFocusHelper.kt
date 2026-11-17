package com.simprints.infra.camera.helpers

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.MeteringPoint
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.view.PreviewView
import androidx.lifecycle.Observer
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@ExcludedFromGeneratedTestCoverageReports(
    reason = "These are UI utilities for focus controls in the camera preview",
)
internal class CameraFocusHelper @AssistedInject constructor(
    @Assisted private val crashReportTag: LoggingConstants.CrashReportTag,
) {
    @AssistedFactory
    interface Factory {
        fun create(crashReportTag: LoggingConstants.CrashReportTag): CameraFocusHelper
    }

    @SuppressLint("ClickableViewAccessibility")
    /**
     * @param exposureTarget when set, exposure and white balance stay metered on this area (in preview coordinates)
     *        rather than following the tapped point
     */
    fun setUpFocusOnTap(
        cameraPreview: PreviewView,
        camera: Camera,
        exposureTarget: Rect? = null,
    ) {
        cameraPreview.afterMeasured {
            it.setOnTouchListener(touchListener(it, camera.cameraControl, exposureTarget))
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @ExcludedFromGeneratedTestCoverageReports("Generates inner class of excluded file")
    private fun touchListener(
        previewView: PreviewView,
        cameraControl: CameraControl,
        exposureTarget: Rect?,
    ) = View.OnTouchListener { view, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> true

            MotionEvent.ACTION_UP -> {
                val focusPoint = getFocusOnTapPoint(view, event)

                val focusAction = FocusMeteringAction
                    .Builder(
                        focusPoint,
                        FocusMeteringAction.FLAG_AF,
                    ).apply {
                        // A new action replaces the previous one, so keep metering exposure on the target
                        exposureTarget?.let { addPoint(getTargetPoint(previewView, it), FLAGS_AE_AWB) }
                    }.disableAutoCancel()
                    .build()

                try {
                    cameraControl.startFocusAndMetering(focusAction)
                } catch (e: CameraInfoUnavailableException) {
                    Simber.e("Cannot access camera", e, tag = crashReportTag)
                }
                true
            }

            else -> false
        }
    }

    fun setUpAutoFocus(
        cameraPreview: PreviewView,
        camera: Camera,
    ) {
        cameraPreview.afterMeasured {
            val focusPoint = getAutoFocusPoint(it)

            val focusAction = FocusMeteringAction
                .Builder(
                    focusPoint,
                    FocusMeteringAction.FLAG_AF,
                ).setAutoCancelDuration(1, TimeUnit.SECONDS)
                .build()

            try {
                camera.cameraControl.startFocusAndMetering(focusAction)
            } catch (e: CameraInfoUnavailableException) {
                Simber.e("Cannot access camera", e, tag = crashReportTag)
            }
        }
    }

    /**
     * Meters auto-exposure and auto-white-balance on [target] (in preview coordinates) instead of the whole frame,
     * so a subject in the target is exposed correctly against a bright or dark background.
     * Autofocus is not part of the action, so it stays continuous.
     */
    fun setUpExposureOnTarget(
        cameraPreview: PreviewView,
        camera: Camera,
        target: Rect,
    ) {
        // The preview's metering point factory maps view coordinates through the preview's rotation and crop,
        // which are only known once the preview is streaming
        cameraPreview.afterStreaming {
            val meteringAction = FocusMeteringAction
                .Builder(getTargetPoint(it, target), FLAGS_AE_AWB)
                .disableAutoCancel()
                .build()

            try {
                camera.cameraControl.startFocusAndMetering(meteringAction)
            } catch (e: CameraInfoUnavailableException) {
                Simber.e("Cannot access camera", e, tag = crashReportTag)
            }
        }
    }

    private inline fun PreviewView.afterStreaming(crossinline block: (previewView: PreviewView) -> Unit) {
        previewStreamState.observeForever(
            @ExcludedFromGeneratedTestCoverageReports("Inner class of excluded file")
            object : Observer<PreviewView.StreamState> {
                override fun onChanged(value: PreviewView.StreamState) {
                    if (value == PreviewView.StreamState.STREAMING) {
                        previewStreamState.removeObserver(this)
                        block(this@afterStreaming)
                    }
                }
            },
        )
    }

    private inline fun PreviewView.afterMeasured(crossinline block: (previewView: PreviewView) -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(
            @ExcludedFromGeneratedTestCoverageReports("Inner class of excluded file")
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (measuredWidth > 0 && measuredHeight > 0) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        block(this@afterMeasured)
                    }
                }
            },
        )
    }

    private fun getFocusOnTapPoint(
        view: View,
        event: MotionEvent,
    ): MeteringPoint = SurfaceOrientedMeteringPointFactory(
        view.width.toFloat(),
        view.height.toFloat(),
    ).createPoint(event.x, event.y)

    private fun getAutoFocusPoint(view: View): MeteringPoint {
        val width = view.width.toFloat()
        val height = view.height.toFloat()

        val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(width, height)
        val centreWidth = width / 2
        val centreHeight = height / 2

        return factory.createPoint(centreWidth, centreHeight)
    }

    /**
     * The point size applies to both sensor dimensions, so it is taken from the target's smaller share of the preview
     * to keep the metering region inside the target.
     */
    private fun getTargetPoint(
        previewView: PreviewView,
        target: Rect,
    ): MeteringPoint {
        val size = minOf(
            target.width() / previewView.width.toFloat(),
            target.height() / previewView.height.toFloat(),
        ).coerceIn(0f, 1f)
        return previewView.meteringPointFactory.createPoint(target.exactCenterX(), target.exactCenterY(), size)
    }

    private companion object {
        private const val FLAGS_AE_AWB = FocusMeteringAction.FLAG_AE or FocusMeteringAction.FLAG_AWB
    }
}
