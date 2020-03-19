package com.simprints.id.activities.qrcapture.tools

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import androidx.camera.core.*
import androidx.camera.view.PreviewView
import timber.log.Timber

class CameraFocusManagerImpl : CameraFocusManager {

    @SuppressLint("ClickableViewAccessibility")
    override fun setUpFocusOnTap(cameraPreview: PreviewView, camera: Camera) {
        cameraPreview.afterMeasured {
            cameraPreview.setOnTouchListener { view, event ->
                return@setOnTouchListener when (event.action) {
                    MotionEvent.ACTION_DOWN -> true

                    MotionEvent.ACTION_UP -> {
                        val focusPoint = getFocusPoint(view, event)

                        try {
                            camera.cameraControl.startFocusAndMetering(
                                FocusMeteringAction.Builder(
                                    focusPoint,
                                    FocusMeteringAction.FLAG_AF
                                ).apply {
                                    disableAutoCancel()
                                }.build()
                            )
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

    private inline fun PreviewView.afterMeasured(crossinline block: () -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    block()
                }
            }
        })
    }

    private fun getFocusPoint(view: View, event: MotionEvent): MeteringPoint {
        val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
            view.width.toFloat(), view.height.toFloat()
        )

        return factory.createPoint(event.x, event.y)
    }

}
