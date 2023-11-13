package com.simprints.face.capture.confirmation

import android.graphics.Bitmap
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.FlowProvider
import com.simprints.face.capture.R
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.capture.screens.FaceCaptureViewModel
import com.simprints.face.capture.screens.confirmation.ConfirmationFragment
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.simprints.infra.resources.R as IDR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ConfirmationFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val faceCaptureViewModel: FaceCaptureViewModel = mockk(relaxed = true) {
        every { getSampleDetection() } returns FaceDetection(
            Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888),
            face = null,
            status = FaceDetection.Status.VALID,
            securedImageRef = null,
            detectionStartTime = 0,
            isFallback = false,
            id = "",
            detectionEndTime = 0
        )
    }

    @Test
    fun onLaunchConfirmationFragmentAssertTextAndNavigation() {
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )

        val confirmationScenario =
            launchFragmentInContainer<ConfirmationFragment>()

        confirmationScenario.onFragment { confirmationFragment ->
            navController.setGraph(R.navigation.graph_face_capture_internal)
            Navigation.setViewNavController(confirmationFragment.requireView(), navController)
        }

        onView(
            allOf(
                withId(R.id.face_confirm_title),
                withText(IDR.string.title_confirmation)
            )
        )
            .check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.confirmation_txt),
                withText(IDR.string.captured_successfully)
            )
        )
            .check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.recapture_btn),
                withText(IDR.string.btn_recapture)
            )
        )
            .check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.confirmation_btn),
                withText(IDR.string.btn_finish)
            )
        )
            .check(matches(isDisplayed()))

        onView(withId(R.id.confirmation_btn)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.facePreparationFragment)
    }
}
