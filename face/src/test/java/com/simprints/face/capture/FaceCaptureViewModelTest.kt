package com.simprints.face.capture

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.simprints.face.controllers.core.crashreport.FaceCrashReportManager
import com.simprints.face.controllers.core.image.FaceImageManager
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class FaceCaptureViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val faceImageManager: FaceImageManager = mockk(relaxed = true)

    private val crashReportManager: FaceCrashReportManager = mockk(relaxed = true)

    private fun buildViewModel(shouldSaveFaceImages: Boolean) = FaceCaptureViewModel(
        maxRetries = 0,
        shouldSaveFaceImages = shouldSaveFaceImages,
        faceImageManager = faceImageManager,
        crashReportManager = crashReportManager
    )

    @Test
    private fun `save face detections should not be called when save flag set to false`() {
        val vm = buildViewModel(false)
        vm.flowFinished()
        verify(exactly = 0) { vm.saveFaceDetections() }
    }

    @Test
    private fun `save face detections should be called when save flag set to true`() {
        val vm = buildViewModel(true)
        vm.flowFinished()
        verify(atLeast = 1) { vm.saveFaceDetections() }
    }
}
