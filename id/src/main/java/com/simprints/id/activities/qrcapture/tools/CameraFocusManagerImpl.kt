package com.simprints.id.activities.qrcapture.tools

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import androidx.camera.core.*
import androidx.camera.view.PreviewView
import timber.log.Timber
import java.util.concurrent.TimeUnit

class CameraFocusManagerImpl : CameraFocusManager {

    @SuppressLint("ClickableViewAccessibility")
    override fun setUpFocusOnTap(cameraPreview: PreviewView, camera: Camera) {
        cameraPreview.afterMeasured {
            it.setOnTouchListener { view, event ->
                return@setOnTouchListener when (event.action) {
                    MotionEvent.ACTION_DOWN -> true

                    MotionEvent.ACTION_UP -> {
                        val focusPoint = getFocusOnTapPoint(view, event)

                        val focusAction = FocusMeteringAction.Builder(
                            focusPoint,
                            FocusMeteringAction.FLAG_AF
                        ).disableAutoCancel().build()

                        try {
                            camera.cameraControl.startFocusAndMetering(focusAction)
                        } catch (e: CameraInfoUnavailableException) {
                            Timber.e(e, "Cannot access camera")
                        }
                        true
                    }

                    else -> false
                }
            }
        }
    }

    override fun setUpAutoFocus(cameraPreview: PreviewView, camera: Camera) {
        cameraPreview.afterMeasured {
            val focusPoint = getAutoFocusPoint(it)

            val focusAction = FocusMeteringAction.Builder(
                focusPoint,
                FocusMeteringAction.FLAG_AF
            ).setAutoCancelDuration(1, TimeUnit.SECONDS).build()

            try {
                camera.cameraControl.startFocusAndMetering(focusAction)
            } catch (e: CameraInfoUnavailableException) {
                Timber.e(e, "Cannot access camera")
            }
        }
    }

    private inline fun PreviewView.afterMeasured(
        crossinline block: (previewView: PreviewView) -> Unit
    ) {
        viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (measuredWidth > 0 && measuredHeight > 0) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        block(this@afterMeasured)
                    }
                }
            })
    }

    private fun getFocusOnTapPoint(view: View, event: MotionEvent): MeteringPoint {
        val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
            view.width.toFloat(), view.height.toFloat()
        )

        return factory.createPoint(event.x, event.y)
    }

    private fun getAutoFocusPoint(view: View): MeteringPoint {
        val width = view.width.toFloat()
        val height = view.height.toFloat()

        val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(width, height)
        val centreWidth = width / 2
        val centreHeight = height / 2

        return factory.createPoint(centreWidth, centreHeight)
    }

}
