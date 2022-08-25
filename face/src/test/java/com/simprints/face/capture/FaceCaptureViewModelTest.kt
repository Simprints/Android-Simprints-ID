package com.simprints.face.capture

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.simprints.face.controllers.core.image.FaceImageManager
import com.simprints.face.models.FaceDetection
import com.simprints.infra.config.domain.models.FaceConfiguration.ImageSavingStrategy
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

    private val faceDetections = listOf<FaceDetection>(
        mockk(relaxed = true) {
            every { id } returns "FAKE_ID"
            every { frame } returns mockk {
                every { toByteArray(any()) } returns byteArrayOf()
            }
        }
    )

    private fun buildViewModel(savingStrategy: ImageSavingStrategy) =
        FaceCaptureViewModel(
            configManager = mockk {
                coEvery { getProjectConfiguration() } returns mockk {
                    every { face } returns mockk {
                        every { imageSavingStrategy } returns savingStrategy
                    }
                }
            },
            faceImageManager = faceImageManager
        )

    @Test
    fun `save face detections should not be called when image saving strategy set to NEVER`() {
        testCoroutineRule.runBlockingTest {
            val vm = buildViewModel(ImageSavingStrategy.NEVER)
            vm.captureFinished(faceDetections)
            vm.flowFinished()
            coVerify(exactly = 0) { faceImageManager.save(any(), any()) }
        }
    }

    @Test
    fun `save face detections should be called when image saving strategy set to ONLY_GOO_SCAN`() {
        testCoroutineRule.runBlockingTest {
            val vm = buildViewModel(ImageSavingStrategy.ONLY_GOOD_SCAN)
            vm.captureFinished(faceDetections)
            vm.flowFinished()
            coVerify(atLeast = 1) { faceImageManager.save(any(), any()) }
        }
    }
}
