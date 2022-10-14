package com.simprints.face.exitform

import com.google.common.truth.Truth.assertThat
import com.simprints.face.controllers.core.events.FaceSessionEventsManagerImpl
import com.simprints.face.controllers.core.events.model.RefusalAnswer
import com.simprints.face.controllers.core.events.model.RefusalEvent
import com.simprints.face.controllers.core.timehelper.FaceTimeHelperImpl
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExitFormViewModelTest {

    private val faceSessionEventsMgr = mockk<FaceSessionEventsManagerImpl>(relaxed = true)
    private lateinit var viewModel: ExitFormViewModel
    private val faceTimeHelper = mockk<FaceTimeHelperImpl>(relaxed = true)

    @Before
    fun setUp() {
        viewModel = ExitFormViewModel(faceSessionEventsMgr, faceTimeHelper)
    }

    @Test
    fun `calling save form data assert event is saved to the repo`() {
        every { faceTimeHelper.now() } returns 5L
        viewModel.reason = RefusalAnswer.OTHER
        viewModel.exitFormStartTime = 1L
        viewModel.submitExitForm("Some other reason")

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
        viewModel.reason = null
        viewModel.submitExitForm("")

        verify(exactly = 0) { faceSessionEventsMgr.addEventInBackground(any()) }
    }

    @Test
    fun `calling submitExitForm should update the live data`() {
        every { faceTimeHelper.now() } returns 5L
        viewModel.reason = RefusalAnswer.OTHER
        viewModel.submitExitForm("Some other reason")

        val response = viewModel.finishFlowWithExitFormEvent.getOrAwaitValue().peekContent()
        assertThat(response.reason).isEqualTo(RefusalAnswer.OTHER)
        assertThat(response.extra).isEqualTo("Some other reason")

    }
}
