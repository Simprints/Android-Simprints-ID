package com.simprints.feature.dashboard.main.projectdetails

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.tools.MviStubs
import com.simprints.feature.dashboard.tools.stubMvi
import com.simprints.testtools.hilt.launchFragmentInHiltContainer
import com.simprints.testtools.hilt.moveToState
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.simprints.infra.resources.R as IDR

private val LOADED_STATE = ProjectDetailsUiState(
    isLoaded = true,
    title = "Project X",
    lastUser = "user-1",
    lastScanner = "SP1",
)

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ProjectDetailsFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val viewModel = mockk<ProjectDetailsViewModel>(relaxed = true)

    private lateinit var mvi: MviStubs<ProjectDetailsUiState, ProjectDetailsUiEffect>

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setUp() {
        mvi = viewModel.stubMvi(ProjectDetailsUiState())
    }

    @Test
    fun `should hide the scanner version text if empty`() {
        mvi.state.value = LOADED_STATE
        launchFragmentInHiltContainer<ProjectDetailsFragment>()
        onView(withId(R.id.dashboard_project_details_card_scanner_used)).check(matches(isDisplayed()))

        mvi.state.value = LOADED_STATE.copy(lastScanner = "")

        onView(withId(R.id.dashboard_project_details_card)).check(matches(isDisplayed()))
        onView(withId(R.id.dashboard_project_details_card_scanner_used)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should display the scanner version text if not empty`() {
        mvi.state.value = LOADED_STATE.copy(lastScanner = "SP56743526")

        launchFragmentInHiltContainer<ProjectDetailsFragment>()

        onView(withId(R.id.dashboard_project_details_card_scanner_used))
            .check(matches(isDisplayed()))
            .check(matches(withText(context.getString(IDR.string.dashboard_project_card_scanner_used, "SP56743526"))))
    }

    @Test
    fun `loaded state shows the card with the title, current user and scanner`() {
        mvi.state.value = LOADED_STATE

        launchFragmentInHiltContainer<ProjectDetailsFragment>()

        assertLoadedStateRendered(title = "Project X", user = "user-1", scanner = "SP1")
    }

    @Test
    fun `a later loaded state replaces the rendered texts`() {
        mvi.state.value = LOADED_STATE
        launchFragmentInHiltContainer<ProjectDetailsFragment>()

        mvi.state.value = ProjectDetailsUiState(isLoaded = true, title = "Project Y", lastUser = "user-2", lastScanner = "SP2")

        assertLoadedStateRendered(title = "Project Y", user = "user-2", scanner = "SP2")
    }

    @Test
    fun `not loaded state hides a card that was shown and a later load shows it again`() {
        mvi.state.value = LOADED_STATE
        launchFragmentInHiltContainer<ProjectDetailsFragment>()
        onView(withId(R.id.dashboard_project_details_card)).check(matches(isDisplayed()))

        mvi.state.value = ProjectDetailsUiState(isLoaded = false)

        onView(withId(R.id.dashboard_project_details_card)).check(matches(not(isDisplayed())))

        mvi.state.value = LOADED_STATE

        onView(withId(R.id.dashboard_project_details_card)).check(matches(isDisplayed()))
    }

    @Test
    fun `no state yet keeps the card xml defaults`() {
        launchFragmentInHiltContainer<ProjectDetailsFragment>()

        onView(withId(R.id.dashboard_project_details_card)).check(matches(isDisplayed()))
        onView(withId(R.id.dashboard_project_details_card_title)).check(matches(withText("")))
        onView(withId(R.id.dashboard_project_details_card_current_user)).check(matches(withText("")))
        onView(withId(R.id.dashboard_project_details_card_scanner_used))
            .check(matches(isDisplayed()))
            .check(matches(withText("")))
    }

    @Test
    fun `every resume sends a load`() {
        launchFragmentInHiltContainer<ProjectDetailsFragment> {
            verify(exactly = 1) { viewModel.onAction(ProjectDetailsUiAction.Load) }

            requireActivity().moveToState(Lifecycle.State.STARTED)
            verify(exactly = 1) { viewModel.onAction(ProjectDetailsUiAction.Load) }

            requireActivity().moveToState(Lifecycle.State.RESUMED)
        }

        verify(exactly = 2) { viewModel.onAction(ProjectDetailsUiAction.Load) }
    }

    private fun assertLoadedStateRendered(
        title: String,
        user: String,
        scanner: String,
    ) {
        onView(withId(R.id.dashboard_project_details_card)).check(matches(isDisplayed()))
        onView(withId(R.id.dashboard_project_details_card_title)).check(matches(withText(title)))
        onView(withId(R.id.dashboard_project_details_card_current_user))
            .check(matches(withText(context.getString(IDR.string.dashboard_project_card_current_user, user))))
        onView(withId(R.id.dashboard_project_details_card_scanner_used))
            .check(matches(isDisplayed()))
            .check(matches(withText(context.getString(IDR.string.dashboard_project_card_scanner_used, scanner))))
    }
}
