package com.simprints.feature.login.tools.camera

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.MeteringPoint
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.view.PreviewView
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.LOGIN
import com.simprints.infra.logging.Simber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ExcludedFromGeneratedTestCoverageReports(
    reason = "These are UI utilities for focus controls in the camera preview",
)
internal class CameraFocusManager @Inject constructor() {
    @SuppressLint("ClickableViewAccessibility")
    fun setUpFocusOnTap(
        cameraPreview: PreviewView,
        camera: Camera,
    ) {
        cameraPreview.afterMeasured {
            it.setOnTouchListener(touchListener(camera.cameraControl))
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @ExcludedFromGeneratedTestCoverageReports("Generates inner class of excluded file")
    private fun touchListener(cameraControl: CameraControl) = OnTouchListener { view, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> true

            MotionEvent.ACTION_UP -> {
                val focusPoint = getFocusOnTapPoint(view, event)

                val focusAction = FocusMeteringAction
                    .Builder(
                        focusPoint,
                        FocusMeteringAction.FLAG_AF,
                    ).disableAutoCancel()
                    .build()

                try {
                    cameraControl.startFocusAndMetering(focusAction)
                } catch (e: CameraInfoUnavailableException) {
                    Simber.e("Cannot access camera", e, tag = LOGIN)
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
                Simber.e("Cannot access camera", e, tag = LOGIN)
            }
        }
    }

    private inline fun PreviewView.afterMeasured(crossinline block: (previewView: PreviewView) -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(
            @ExcludedFromGeneratedTestCoverageReports("Inner class of excluded file")
            object : OnGlobalLayoutListener {
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
    ): MeteringPoint = SurfaceOrientedMeteringPointFactory(view.width.toFloat(), view.height.toFloat())
        .createPoint(event.x, event.y)

    private fun getAutoFocusPoint(view: View): MeteringPoint {
        val width = view.width.toFloat()
        val height = view.height.toFloat()

        val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(width, height)
        val centreWidth = width / 2
        val centreHeight = height / 2

        return factory.createPoint(centreWidth, centreHeight)
    }
}
