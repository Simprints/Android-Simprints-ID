package com.simprints.face.capture.livefeedback

import android.os.Build
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import com.simprints.face.R
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.logging.Simber
import io.mockk.mockk
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest

@RunWith(AndroidJUnit4::class)
class LiveFeedbackFragmentTest : KoinTest {

    private lateinit var device: UiDevice
    private val faceCaptureViewModel: FaceCaptureViewModel = mockk(relaxed = true)
    private val liveFeedBackVm: LiveFeedbackFragmentViewModel = mockk(relaxed = true)

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        loadKoinModules(
            module(override = true) {
                viewModel { faceCaptureViewModel }
                viewModel { liveFeedBackVm }
            })
    }

    @Test
    fun openingLiveFeedBackScreenShowsCorrectText() {
        val navController: NavHostController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        val liveFeedBackScenario =
            launchFragmentInContainer<LiveFeedbackFragment>()

        liveFeedBackScenario.onFragment { liveFeedbackFragment ->
            navController.setGraph(R.navigation.capture_graph)
            Navigation.setViewNavController(liveFeedbackFragment.requireView(), navController)
        }
        allowPermissionsIfNeeded("Only this time")
        allowPermissionsIfNeeded("Allow")
        // Is this test really useful for the UI??
        onView(
            allOf(
                withId(R.id.capture_feedback_txt_title),
                withText(R.string.capture_title_previewing)
            )
        )
            .check(matches(isDisplayed()))
    }

    private fun allowPermissionsIfNeeded(text: String) {
        if (Build.VERSION.SDK_INT >= 23) {
            val allowPermissions = device.findObject(UiSelector().text(text))
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click()
                } catch (e: UiObjectNotFoundException) {
                    Simber.e(e, "There is no permissions dialog to interact with.")
                }
            }
        }
    }
}

