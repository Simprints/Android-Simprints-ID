package com.simprints.face.capture.confirmation

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.face.R
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import com.simprints.face.models.FaceDetection
import com.simprints.uicomponents.models.PreviewFrame
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class ConfirmationFragmentTest {

    private val faceCaptureViewModel: FaceCaptureViewModel = mockk(relaxed = true) {
        every {
            faceDetections
        } returns listOf(
            FaceDetection(
                frame = PreviewFrame(
                    width = 100,
                    height = 100,
                    format = 0,
                    mirrored = false,
                    bytes = byteArrayOf()
                ),
                face = null,
                status = FaceDetection.Status.VALID,
                securedImageRef = null,
                detectionStartTime = 0,
                isFallback = false,
                id = "",
                detectionEndTime = 0
            )
        )
    }

    private val faceTimeHelper: FaceTimeHelper = mockk(relaxed = true)
    private val faceSessionEventsManager: FaceSessionEventsManager = mockk(relaxed = true)

    @Before
    fun setUp() {
        loadKoinModules(module(override = true) {
            viewModel { faceCaptureViewModel }
            single { faceTimeHelper }
            single { faceSessionEventsManager }
        })
    }

    @Test
    fun onLaunchConfirmationFragmentAssertTextAndNavigation() {
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )

        val confirmationScenario =
            launchFragmentInContainer<ConfirmationFragment>()

        confirmationScenario.onFragment { confirmationFragment ->
            navController.setGraph(R.navigation.capture_graph)
            Navigation.setViewNavController(confirmationFragment.requireView(), navController)
        }

        onView(
            allOf(
                withId(R.id.face_confirm_title),
                withText(R.string.title_confirmation)
            )
        )
            .check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.confirmation_txt),
                withText(R.string.captured_successfully)
            )
        )
            .check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.recapture_btn),
                withText(R.string.btn_recapture)
            )
        )
            .check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.confirmation_btn),
                withText(R.string.btn_finish)
            )
        )
            .check(matches(isDisplayed()))

        onView(withId(R.id.confirmation_btn)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.liveFeedbackFragment)
    }
}
