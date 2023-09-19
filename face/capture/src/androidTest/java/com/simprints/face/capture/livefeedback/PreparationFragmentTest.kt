package com.simprints.face.capture.livefeedback

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.face.R
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import com.simprints.face.capture.screens.preparation.PreparationFragment
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PreparationFragmentTest {

    private val faceTimeHelper: FaceTimeHelper = mockk(relaxed = true)
    private val faceSessionEventsManager: FaceSessionEventsManager = mockk(relaxed = true)

    @Test
    fun testNavigationFromPreparationToLiveFeedBackFragment() {
        val navController: NavHostController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        val prepFragScenario =
            launchFragmentInContainer<PreparationFragment>()

        prepFragScenario.onFragment { prepFragment ->
            navController.setGraph(R.navigation.face_capture_graph)
            Navigation.setViewNavController(prepFragment.requireView(), navController)
        }

        onView(withId(R.id.detection_onboarding_frame)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.liveFeedbackFragment)
    }
}
