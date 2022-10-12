package com.simprints.face.capture.confirmation

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.google.common.truth.Truth.assertThat
import com.simprints.face.R
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import com.simprints.face.utils.mockFaceDetectionList
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.robolectric.annotation.Config

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class ConfirmationFragmentTest {

    @BindValue
    @JvmField
    val faceCaptureViewModel: FaceCaptureViewModel = mockk(relaxed = true) {
        every {
            faceDetections
        } returns mockFaceDetectionList
    }

    @BindValue
    @JvmField
    val faceTimeHelper: FaceTimeHelper = mockk(relaxed = true)

    @BindValue
    @JvmField
    val faceSessionEventsManager: FaceSessionEventsManager = mockk(relaxed = true)


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
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.preparationFragment)
    }
}
