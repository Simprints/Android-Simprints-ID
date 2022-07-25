package com.simprints.face.exitform

import com.google.common.truth.Truth.assertThat
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.controllers.core.events.FaceSessionEventsManagerImpl
import com.simprints.face.controllers.core.events.model.RefusalAnswer
import com.simprints.face.controllers.core.events.model.RefusalEvent
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class ExitFormViewModelTest {

    private val faceSessionEventsMgr = mockk<FaceSessionEventsManagerImpl>(relaxed = true)
    private val mainVm = mockk<FaceCaptureViewModel>(relaxed = true)
    private lateinit var viewModel: ExitFormViewModel

    @Before
    fun setUp() {
        viewModel = ExitFormViewModel(mainVm, faceSessionEventsMgr)
    }

    @Test
    fun `calling save form data assert event is saved to the repo`() {
        viewModel.exitFormData = Pair(RefusalAnswer.OTHER, "Some other reason")
        viewModel.logExitFormEvent(1L, 5L)

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
        viewModel.logExitFormEvent(0L, 2L)

        verify(exactly = 0) { faceSessionEventsMgr.addEventInBackground(any()) }
    }
}
