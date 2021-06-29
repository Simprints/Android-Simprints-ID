package com.simprints.face.capture.livefeedback

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
import androidx.test.rule.GrantPermissionRule
import com.simprints.face.R
import com.simprints.face.capture.FaceCaptureViewModel
import io.mockk.mockk
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest

@RunWith(AndroidJUnit4::class)
class LiveFeedbackFragmentTest : KoinTest {

    @Rule
    @JvmField
    val grantPermissions: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val faceCaptureViewModel: FaceCaptureViewModel = mockk(relaxed = true)
    private val liveFeedBackVm: LiveFeedbackFragmentViewModel = mockk(relaxed = true)

    @Before
    fun setUp() {
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
            launchFragmentInContainer<LiveFeedbackFragment>(themeResId = com.simprints.id.R.style.AppTheme)

        liveFeedBackScenario.onFragment { liveFeedbackFragment ->
            navController.setGraph(R.navigation.capture_graph)
            Navigation.setViewNavController(liveFeedbackFragment.requireView(), navController)
        }

        // Is this test really useful for the UI??
        onView(
            allOf(
                withId(R.id.capture_feedback_txt_title),
                withText(R.string.capture_title_previewing)
            )
        )
            .check(matches(isDisplayed()))
    }
}

