package com.simprints.feature.dashboard.main.projectdetails

import androidx.lifecycle.Observer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.feature.dashboard.R
import com.simprints.testtools.hilt.launchFragmentInHiltContainer
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.core.IsNot.not
import org.hamcrest.core.StringContains.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class ProjectDetailsFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val viewModel = mockk<ProjectDetailsViewModel>(relaxed = true)

    @Test
    fun `should hide the scanner version text if empty`() {
        mockProjectState("")

        launchFragmentInHiltContainer<ProjectDetailsFragment>()

        onView(withId(R.id.dashboard_project_details_card_scanner_used)).check(
            matches(not(isDisplayed())),
        )
    }

    @Test
    fun `should display the scanner version text if not empty`() {
        mockProjectState("SP56743526")

        launchFragmentInHiltContainer<ProjectDetailsFragment>()

        onView(withId(R.id.dashboard_project_details_card_scanner_used)).check(
            matches(withText(containsString("SP56743526"))),
        )
    }

    private fun mockProjectState(lastScanner: String) {
        every { viewModel.projectCardStateLiveData } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<DashboardProjectState>>().onChanged(
                    DashboardProjectState(
                        "",
                        "",
                        lastScanner,
                    ),
                )
            }
        }
    }
}
