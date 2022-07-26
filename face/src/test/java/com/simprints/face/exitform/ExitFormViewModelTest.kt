package com.simprints.face.exitform

import com.google.common.truth.Truth.assertThat
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.controllers.core.events.FaceSessionEventsManagerImpl
import com.simprints.face.controllers.core.events.model.RefusalAnswer
import com.simprints.face.controllers.core.events.model.RefusalEvent
import com.simprints.face.controllers.core.timehelper.FaceTimeHelperImpl
import io.mockk.*
import org.junit.Before
import org.junit.Test

class ExitFormViewModelTest {

    private val faceSessionEventsMgr = mockk<FaceSessionEventsManagerImpl>(relaxed = true)
    private val faceCaptureViewModel = mockk<FaceCaptureViewModel>(relaxed = true)
    private lateinit var viewModel: ExitFormViewModel
    private val faceTimeHelper = mockk<FaceTimeHelperImpl>(relaxed = true)

    @Before
    fun setUp() {
        viewModel = ExitFormViewModel(faceCaptureViewModel, faceSessionEventsMgr, faceTimeHelper)
    }

    @Test
    fun `calling save form data assert event is saved to the repo`() {
        every { faceTimeHelper.now() } returns 5L
        viewModel.exitFormData = Pair(RefusalAnswer.OTHER, "Some other reason")
        viewModel.exitFormStartTime = 1L
        viewModel.logExitFormEvent()

        verify(exactly = 1) {
            faceSessionEventsMgr.addEventInBackground(match {
                with(it as RefusalEvent) {
                    assertThat(startTime).isEqualTo(1L)
                    assertThat(endTime).isEqualTo(5L)
                    assertThat(reason).isEqualTo(RefusalAnswer.OTHER)
                    assertThat(otherText).isEqualTo("Some other reason")
                }
                true
            })
        }
    }

    @Test
    fun `calling save form data assert event is not saved with null exit data`() {
        viewModel.exitFormData = null
        viewModel.logExitFormEvent()

        verify(exactly = 0) { faceSessionEventsMgr.addEventInBackground(any()) }
    }
}
