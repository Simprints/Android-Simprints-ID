package com.simprints.feature.dashboard.main.dailyactivity

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.main.dailyactivity.DailyActivityUiState.DailyActivityUi
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

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class DailyActivityFragmentTest {
    companion object {
        private const val DATE = "2022-11-15"
        private const val OTHER_DATE = "2022-11-16"
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val viewModel = mockk<DailyActivityViewModel>(relaxed = true)

    private lateinit var mvi: MviStubs<DailyActivityUiState, DailyActivityUiEffect>

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setUp() {
        mvi = viewModel.stubMvi(DailyActivityUiState())
    }

    @Test
    fun `should hide the card when the daily activity is empty`() {
        setDailyActivity(enrolments = 2)
        launchFragmentInHiltContainer<DailyActivityFragment>()
        onView(withId(R.id.dashboard_daily_activity_card)).check(matches(isDisplayed()))

        setDailyActivity()

        onView(withId(R.id.dashboard_daily_activity_card)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should display the card when the daily activity is not empty`() {
        setDailyActivity(enrolments = 2)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.dashboard_daily_activity_card)).check(matches(isDisplayed()))
    }

    @Test
    fun `should include the date in the title`() {
        setDailyActivity(enrolments = 2)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.dashboard_daily_activity_card_title))
            .check(matches(withText(context.getString(IDR.string.dashboard_activity_card_activity, DATE))))
    }

    @Test
    fun `should hide the enrolment when the enrolment count is 0`() {
        setDailyActivity(enrolments = 2, identifications = 2)
        launchFragmentInHiltContainer<DailyActivityFragment>()
        onView(withId(R.id.group_enrolments)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        setDailyActivity(enrolments = 0, identifications = 2)

        onView(withId(R.id.group_enrolments)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun `should display the enrolment when the enrolment count is not 0`() {
        setDailyActivity(enrolments = 2, identifications = 2)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.group_enrolments)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.dashboard_daily_activity_card_enrolments_count)).check(matches(withText("2")))
    }

    @Test
    fun `should hide the identification when the identification count is 0`() {
        setDailyActivity(enrolments = 2, identifications = 2)
        launchFragmentInHiltContainer<DailyActivityFragment>()
        onView(withId(R.id.group_identifications)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        setDailyActivity(enrolments = 2, identifications = 0)

        onView(withId(R.id.group_identifications)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun `should display the identification when the identification count is not 0`() {
        setDailyActivity(enrolments = 2, identifications = 2)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.group_identifications)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.dashboard_daily_activity_card_identifications_count)).check(matches(withText("2")))
    }

    @Test
    fun `should hide the verification when the verification count is 0`() {
        setDailyActivity(enrolments = 2, verifications = 2)
        launchFragmentInHiltContainer<DailyActivityFragment>()
        onView(withId(R.id.group_verifications)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        setDailyActivity(enrolments = 2, verifications = 0)

        onView(withId(R.id.group_verifications)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun `should display the verification when the verification count is not 0`() {
        setDailyActivity(enrolments = 2, verifications = 2)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.group_verifications)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.dashboard_daily_activity_card_verifications_count)).check(matches(withText("2")))
    }

    @Test
    fun `should hide both dividers if only the enrolment is not 0`() {
        launchWithBothDividersShown()

        setDailyActivity(enrolments = 2)

        assertDividers(enrolmentsShown = false, identificationsShown = false)
    }

    @Test
    fun `should hide both dividers if only the identification is not 0`() {
        launchWithBothDividersShown()

        setDailyActivity(identifications = 2)

        assertDividers(enrolmentsShown = false, identificationsShown = false)
    }

    @Test
    fun `should hide both dividers if only the verification is not 0`() {
        launchWithBothDividersShown()

        setDailyActivity(verifications = 2)

        assertDividers(enrolmentsShown = false, identificationsShown = false)
    }

    @Test
    fun `should display a divider if the enrolment and identification are not 0`() {
        launchWithBothDividersShown()

        setDailyActivity(enrolments = 2, identifications = 3)

        assertDividers(enrolmentsShown = true, identificationsShown = false)
    }

    @Test
    fun `should display a divider if the enrolment and verification are not 0`() {
        launchWithBothDividersShown()

        setDailyActivity(enrolments = 2, verifications = 3)

        assertDividers(enrolmentsShown = true, identificationsShown = false)
    }

    @Test
    fun `should display a divider if the identification and verification are not 0`() {
        launchWithBothDividersShown()

        setDailyActivity(identifications = 2, verifications = 3)

        assertDividers(enrolmentsShown = false, identificationsShown = true)
    }

    @Test
    fun `should display both dividers if the enrolment, identification and verification are not 0`() {
        setDailyActivity(enrolments = 1, identifications = 2, verifications = 3)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        assertDividers(enrolmentsShown = true, identificationsShown = true)
    }

    @Test
    fun `a state without activity hides the card that showed every group`() {
        setDailyActivity(enrolments = 2, identifications = 3, verifications = 4)
        launchFragmentInHiltContainer<DailyActivityFragment>()
        onView(withId(R.id.dashboard_daily_activity_card)).check(matches(isDisplayed()))

        setDailyActivity()

        onView(withId(R.id.dashboard_daily_activity_card)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `enrolments dropping to zero hides its group and divider while identifications stay`() {
        setDailyActivity(enrolments = 2, identifications = 3)
        launchFragmentInHiltContainer<DailyActivityFragment>()
        onView(withId(R.id.group_enrolments)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.divider_enrolments)).check(matches(isDisplayed()))

        setDailyActivity(identifications = 3)

        onView(withId(R.id.dashboard_daily_activity_card)).check(matches(isDisplayed()))
        onView(withId(R.id.group_enrolments)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.divider_enrolments)).check(matches(not(isDisplayed())))
        onView(withId(R.id.group_identifications)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.dashboard_daily_activity_card_identifications_count)).check(matches(withText("3")))
    }

    @Test
    fun `labels use the singular quantity string for a count of one`() {
        setDailyActivity(enrolments = 1, identifications = 1, verifications = 1)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        assertLabels(count = 1)
    }

    @Test
    fun `labels switch to the plural quantity string when the count becomes two`() {
        setDailyActivity(enrolments = 1, identifications = 1, verifications = 1)
        launchFragmentInHiltContainer<DailyActivityFragment>()

        setDailyActivity(enrolments = 2, identifications = 2, verifications = 2)

        assertLabels(count = 2)
    }

    @Test
    fun `a later state replaces the date and the counts`() {
        setDailyActivity(enrolments = 2)
        launchFragmentInHiltContainer<DailyActivityFragment>()

        setDailyActivity(enrolments = 5, date = OTHER_DATE)

        onView(withId(R.id.dashboard_daily_activity_card_title))
            .check(matches(withText(context.getString(IDR.string.dashboard_activity_card_activity, OTHER_DATE))))
        onView(withId(R.id.dashboard_daily_activity_card_enrolments_count)).check(matches(withText("5")))
    }

    @Test
    fun `no state yet keeps the card xml defaults`() {
        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.dashboard_daily_activity_card)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.dashboard_daily_activity_card_title)).check(matches(withText("")))
        onView(withId(R.id.group_enrolments)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.enrolments_label)).check(matches(withText("")))
    }

    @Test
    fun `every resume sends a load`() {
        launchFragmentInHiltContainer<DailyActivityFragment> {
            verify(exactly = 1) { viewModel.onAction(DailyActivityUiAction.Load) }

            requireActivity().moveToState(Lifecycle.State.STARTED)
            verify(exactly = 1) { viewModel.onAction(DailyActivityUiAction.Load) }

            requireActivity().moveToState(Lifecycle.State.RESUMED)
        }

        verify(exactly = 2) { viewModel.onAction(DailyActivityUiAction.Load) }
    }

    private fun launchWithBothDividersShown() {
        setDailyActivity(enrolments = 1, identifications = 2, verifications = 3)
        launchFragmentInHiltContainer<DailyActivityFragment>()
        assertDividers(enrolmentsShown = true, identificationsShown = true)
    }

    private fun assertDividers(
        enrolmentsShown: Boolean,
        identificationsShown: Boolean,
    ) {
        onView(withId(R.id.divider_enrolments)).check(matches(if (enrolmentsShown) isDisplayed() else not(isDisplayed())))
        onView(withId(R.id.divider_identifications)).check(matches(if (identificationsShown) isDisplayed() else not(isDisplayed())))
    }

    private fun assertLabels(count: Int) {
        val res = context.resources
        onView(withId(R.id.enrolments_label))
            .check(matches(withText(res.getQuantityString(IDR.plurals.dashboard_activity_card_enrolments, count))))
        onView(withId(R.id.identifications_label))
            .check(matches(withText(res.getQuantityString(IDR.plurals.dashboard_activity_card_identifications, count))))
        onView(withId(R.id.verifications_label))
            .check(matches(withText(res.getQuantityString(IDR.plurals.dashboard_activity_card_verifications, count))))
    }

    private fun setDailyActivity(
        enrolments: Int = 0,
        identifications: Int = 0,
        verifications: Int = 0,
        date: String = DATE,
    ) {
        mvi.state.value = DailyActivityUiState(DailyActivityUi(enrolments, identifications, verifications, date))
    }
}
