package com.simprints.face.capture.livefeedback.tools

import android.graphics.ImageFormat
import android.graphics.RectF
import androidx.core.graphics.toRect
import com.otaliastudios.cameraview.frame.Frame
import com.simprints.uicomponents.models.PreviewFrame
import com.simprints.uicomponents.imageTools.LibYuvJni
import com.simprints.uicomponents.models.Size
import kotlin.math.min

class FrameProcessor(private val libYuvJni: LibYuvJni) {

    fun previewFrameFrom(
        cameraFrame: Frame,
        boxOnTheScreen: RectF,
        screenSize: Size,
        facingFront: Boolean
    ): PreviewFrame {
        val cameraWidth = cameraFrame.size.width
        val cameraHeight = cameraFrame.size.height

        val screenWidth = screenSize.width
        val screenHeight = screenSize.height

        val (rotatedCameraWidth, rotatedCameraHeight) =
            if (cameraFrame.rotation in arrayOf(90, 270)) {
                Pair(cameraFrame.size.height, cameraFrame.size.width)
            } else {
                Pair(cameraFrame.size.width, cameraFrame.size.height)
            }

        val newRectSize =
            if (screenWidth == rotatedCameraWidth || screenHeight == rotatedCameraHeight) {
                val cameraArea = rotatedCameraHeight * rotatedCameraWidth
                val screenArea = screenHeight * screenWidth
                // This means center crop is tight
                if (cameraArea > screenArea) {
                    boxOnTheScreen.width()
                } else {
                    // This means the camera is zooming in to center crop
                    sizeFromMinRatio(
                        rotatedCameraWidth,
                        screenWidth,
                        rotatedCameraHeight,
                        screenHeight,
                        boxOnTheScreen.width()
                    )
                }
            } else {
                // This means the camera is zooming out to center crop
                sizeFromMinRatio(
                    rotatedCameraWidth,
                    screenWidth,
                    rotatedCameraHeight,
                    screenHeight,
                    boxOnTheScreen.width()
                )
            }

        val newBoundingBox =
            CameraTargetOverlay.rectForPlane(rotatedCameraWidth, rotatedCameraHeight, newRectSize)

        val rotatedTargetBoundingBox = when (360 - cameraFrame.rotation) {
            0, 360 -> newBoundingBox
            90 -> RectF(
                cameraWidth - newBoundingBox.bottom,
                newBoundingBox.left,
                cameraWidth - newBoundingBox.top,
                newBoundingBox.right
            )
            180 -> RectF(
                cameraWidth - newBoundingBox.right,
                cameraHeight - newBoundingBox.bottom,
                cameraWidth - newBoundingBox.left,
                cameraHeight - newBoundingBox.top
            )
            270 -> RectF(
                newBoundingBox.top,
                cameraHeight - newBoundingBox.right,
                newBoundingBox.bottom,
                cameraHeight - newBoundingBox.left
            )
            else -> throw IllegalArgumentException("Unsupported rotation angle: ${cameraFrame.rotation}")
        }

        require(cameraFrame.format == ImageFormat.NV21)
        val (croppedFrameSize, croppedFrameBytes) = libYuvJni.cropRotateYuvNV21(
            Size(cameraWidth, cameraHeight), cameraFrame.data,
            rect = rotatedTargetBoundingBox.toRect(),
            rotation = cameraFrame.rotation
        )

        return PreviewFrame(
            croppedFrameSize.width,
            croppedFrameSize.height,
            cameraFrame.format,
            facingFront,
            croppedFrameBytes
        ).also {
            cameraFrame.release()
        }
    }

    private fun sizeFromMinRatio(
        cameraWidth: Int,
        screenWidth: Int,
        cameraHeight: Int,
        screenHeight: Int,
        currentWidth: Float
    ): Float {
        val widthRatio = cameraWidth / screenWidth.toFloat()
        val heightRatio = cameraHeight / screenHeight.toFloat()
        val minRatio = min(widthRatio, heightRatio)
        return currentWidth * minRatio
    }

}
