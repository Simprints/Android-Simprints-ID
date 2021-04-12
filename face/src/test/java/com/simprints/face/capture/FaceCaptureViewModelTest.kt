package com.simprints.face.capture

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.simprints.face.controllers.core.crashreport.FaceCrashReportManager
import com.simprints.face.controllers.core.image.FaceImageManager
import com.simprints.face.models.FaceDetection
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class FaceCaptureViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val faceImageManager: FaceImageManager = mockk(relaxed = true) {
        coEvery { save(any(), any()) } returns null
    }

    private val crashReportManager: FaceCrashReportManager = mockk(relaxed = true)

    private val faceDetections = listOf<FaceDetection>(
        mockk(relaxed = true) {
            every { id } returns "FAKE_ID"
            every { frame } returns mockk {
                every { toByteArray(any()) } returns byteArrayOf()
            }
        }
    )

    private fun buildViewModel(shouldSaveFaceImages: Boolean) = FaceCaptureViewModel(
        maxRetries = 0,
        shouldSaveFaceImages = shouldSaveFaceImages,
        faceImageManager = faceImageManager,
        crashReportManager = crashReportManager
    )

    @Test
    fun `save face detections should not be called when save flag set to false`() {
        val vm = buildViewModel(false)
        vm.captureFinished(faceDetections)
        vm.flowFinished()
        coVerify(exactly = 0) { faceImageManager.save(any(), any()) }
    }

    @Test
    fun `save face detections should be called when save flag set to true`() {
        val vm = buildViewModel(true)
        vm.captureFinished(faceDetections)
        vm.flowFinished()
        coVerify(atLeast = 1) { faceImageManager.save(any(), any()) }
    }
}
