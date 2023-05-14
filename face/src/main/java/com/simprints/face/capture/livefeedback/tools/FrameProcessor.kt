package com.simprints.face.capture.livefeedback.tools

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.util.Size
import androidx.camera.core.ImageProxy
import androidx.core.graphics.toRect
import com.simprints.face.capture.toBitmap
import java.lang.Float.min
import javax.inject.Inject


class FrameProcessor @Inject constructor() {

    private lateinit var screenSize: Size
    private lateinit var boxOnTheScreen: RectF
    private lateinit var cropRect: Rect
    private lateinit var rotationMatrix: Matrix

    fun init(screenSize: Size, boxOnTheScreen: RectF) {
        this.screenSize = screenSize
        this.boxOnTheScreen = boxOnTheScreen
    }

    fun cropRotateFrame(image: ImageProxy): Bitmap {
        if (!this::cropRect.isInitialized) {
            // The cropRect should be calculated once as its value will be the same for all images.
            calcRotatedCropRect(image)
            rotationMatrix = Matrix()
            rotationMatrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
        }
        return image.toBitmap(cropRect, rotationMatrix)

    }

    private fun calcRotatedCropRect(image: ImageProxy) {
        val cameraWidth = image.width
        val cameraHeight = image.height

        val screenWidth = screenSize.width
        val screenHeight = screenSize.height

        val (rotatedCameraWidth, rotatedCameraHeight) = getCameraRotatedPair(image)

        val newRectSize = getRectSizeBasedOnCameraCropping(
            screenWidth,
            screenHeight,
            rotatedCameraWidth,
            rotatedCameraHeight,
            boxOnTheScreen
        )

        val newBoundingBox =
            CameraTargetOverlay.rectForPlane(rotatedCameraWidth, rotatedCameraHeight, newRectSize)

        cropRect = getRotatedBoundingBox(
            image.imageInfo.rotationDegrees,
            newBoundingBox,
            cameraWidth,
            cameraHeight
        ).toRect()
    }

    private fun getRotatedBoundingBox(
        rotation: Int,
        newBoundingBox: RectF,
        cameraWidth: Int,
        cameraHeight: Int
    ): RectF {
        return when (360 - rotation) {
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

            else -> throw IllegalArgumentException("Unsupported rotation angle: ${rotation}")
        }
    }

    private fun getRectSizeBasedOnCameraCropping(
        screenWidth: Int,
        screenHeight: Int,
        cameraWidth: Int,
        cameraHeight: Int,
        boxOnTheScreen: RectF
    ): Float {
        return if (screenWidth == cameraWidth || screenHeight == cameraHeight) {
            val cameraArea = cameraHeight * cameraWidth
            val screenArea = screenHeight * screenWidth
            // This means center crop is tight
            if (cameraArea > screenArea) {
                boxOnTheScreen.width()
            } else {
                // This means the camera is zooming in to center crop
                sizeFromMinRatio(
                    cameraWidth,
                    screenWidth,
                    cameraHeight,
                    screenHeight,
                    boxOnTheScreen.width()
                )
            }
        } else {
            // This means the camera is zooming out to center crop
            sizeFromMinRatio(
                cameraWidth,
                screenWidth,
                cameraHeight,
                screenHeight,
                boxOnTheScreen.width()
            )
        }
    }

    private fun getCameraRotatedPair(image: ImageProxy): Pair<Int, Int> =
        if (cameraRotatedPortrait(image.imageInfo.rotationDegrees)) {
            Pair(image.height, image.width)
        } else {
            Pair(image.width, image.height)
        }

    private fun cameraRotatedPortrait(rotation: Int) = rotation in arrayOf(90, 270)

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
