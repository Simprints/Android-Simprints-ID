package com.simprints.face.capture.livefeedback

import android.os.Build
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import com.simprints.face.capture.R
import com.simprints.face.capture.screens.FaceCaptureViewModel
import com.simprints.face.capture.screens.livefeedback.LiveFeedbackFragment
import com.simprints.face.capture.screens.livefeedback.LiveFeedbackFragmentViewModel
import com.simprints.infra.logging.Simber
import io.mockk.mockk
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.simprints.infra.resources.R as IDR

@RunWith(AndroidJUnit4::class)
class LiveFeedbackFragmentTest {
    private lateinit var device: UiDevice
    private val faceCaptureViewModel: FaceCaptureViewModel = mockk(relaxed = true)
    private val liveFeedBackVm: LiveFeedbackFragmentViewModel = mockk(relaxed = true)

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun openingLiveFeedBackScreenShowsCorrectText() {
        val navController: NavHostController = TestNavHostController(
            ApplicationProvider.getApplicationContext(),
        )
        val liveFeedBackScenario =
            launchFragmentInContainer<LiveFeedbackFragment>()

        liveFeedBackScenario.onFragment { liveFeedbackFragment ->
            navController.setGraph(R.navigation.graph_face_capture_internal)
            Navigation.setViewNavController(liveFeedbackFragment.requireView(), navController)
        }
        allowPermissionsIfNeeded("Only this time")
        allowPermissionsIfNeeded("Allow")
        // Is this test really useful for the UI??
        onView(
            allOf(
                withId(R.id.capture_feedback_txt_title),
                withText(IDR.string.face_capture_title_previewing),
            ),
        ).check(matches(isDisplayed()))
    }

    private fun allowPermissionsIfNeeded(text: String) {
        if (Build.VERSION.SDK_INT >= 23) {
            val allowPermissions = device.findObject(UiSelector().text(text))
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click()
                } catch (e: UiObjectNotFoundException) {
                    Simber.e("There is no permissions dialog to interact with.", e)
                }
            }
        }
    }
}
