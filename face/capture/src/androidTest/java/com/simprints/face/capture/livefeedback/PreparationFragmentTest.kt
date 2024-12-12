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
import com.simprints.face.capture.R
import com.simprints.face.capture.screens.preparation.PreparationFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PreparationFragmentTest {
    @Test
    fun testNavigationFromPreparationToLiveFeedBackFragment() {
        val navController: NavHostController = TestNavHostController(
            ApplicationProvider.getApplicationContext(),
        )
        val prepFragScenario =
            launchFragmentInContainer<PreparationFragment>()

        prepFragScenario.onFragment { prepFragment ->
            navController.setGraph(R.navigation.graph_face_capture_internal)
            Navigation.setViewNavController(prepFragment.requireView(), navController)
        }

        onView(withId(R.id.detection_onboarding_frame)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.faceLiveFeedbackFragment)
    }
}
