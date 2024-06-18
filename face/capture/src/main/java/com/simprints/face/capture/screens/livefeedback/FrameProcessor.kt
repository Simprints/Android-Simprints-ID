package com.simprints.face.capture.screens.livefeedback

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.util.Size
import androidx.camera.core.ImageProxy
import androidx.core.graphics.toRect
import com.simprints.face.capture.models.ScreenOrientation
import com.simprints.face.capture.screens.livefeedback.views.CameraTargetOverlay
import com.simprints.face.capture.usecases.ImageProxyToBitmapUseCase
import java.lang.Float.min
import javax.inject.Inject


internal class FrameProcessor @Inject constructor(
    private val imageProxyToBitmap: ImageProxyToBitmapUseCase,
) {

    private var previewViewWidth: Int = 0
    private var previewViewHeight: Int = 0

    private lateinit var boxOnTheScreen: RectF
    var cropRect: Rect? = null
        private set

    /**
     * Init the frame processor
     *
     * @param previewViewSize the camera preview view size
     * @param boxOnTheScreen the circle target indicator coordinates.
     * we will use this coordinates to compute the area to be cropped for processing
     */
    fun init(previewViewSize: Size, boxOnTheScreen: RectF) {
        previewViewWidth = previewViewSize.width
        previewViewHeight = previewViewSize.height
        this.boxOnTheScreen = boxOnTheScreen
    }

    fun clear() {
        cropRect = null
    }

    /**
     * Extracts part of the image that lays inside
     * the cropRect
     *
     * @param image
     * @return Bitmap
     */
    fun cropRotateFrame(image: ImageProxy, screenOrientation: ScreenOrientation): Bitmap? {
        val cropRect = this.cropRect
            ?: calcRotatedCropRect(image, screenOrientation).also { this.cropRect = it }
        return imageProxyToBitmap(image, cropRect)
    }

    private fun calcRotatedCropRect(image: ImageProxy, screenOrientation: ScreenOrientation): Rect {
        val cameraWidth = image.width
        val cameraHeight = image.height

        val (rotatedCameraWidth, rotatedCameraHeight) = getCameraRotatedPair(image)

        val newRectSize = getRectSizeBasedOnCameraCropping(
            previewViewWidth,
            previewViewHeight,
            rotatedCameraWidth,
            rotatedCameraHeight,
            boxOnTheScreen
        )

        val newBoundingBox = CameraTargetOverlay.rectForPlane(
            width = rotatedCameraWidth,
            height = rotatedCameraHeight,
            rectSize = newRectSize,
            screenOrientation = screenOrientation
        )

        return getRotatedBoundingBox(
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
        cameraHeight: Int,
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
        boxOnTheScreen: RectF,
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
        currentWidth: Float,
    ): Float {
        val widthRatio = cameraWidth / screenWidth.toFloat()
        val heightRatio = cameraHeight / screenHeight.toFloat()
        val minRatio = min(widthRatio, heightRatio)
        return currentWidth * minRatio
    }
}
