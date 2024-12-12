package com.simprints.feature.dashboard.main.dailyactivity

import androidx.lifecycle.Observer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.*
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
class DailyActivityFragmentTest {
    companion object {
        private const val DATE = "2022-11-15"
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val viewModel = mockk<DailyActivityViewModel>(relaxed = true) {
        every { getCurrentDateAsString() } returns DATE
    }

    @Test
    fun `should hide the card when the daily activity is empty`() {
        mockDailyActivity()

        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.dashboard_daily_activity_card)).check(
            matches(not(isDisplayed())),
        )
    }

    @Test
    fun `should display the card when the daily activity is not empty`() {
        mockDailyActivity(enrolments = 2)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.dashboard_daily_activity_card)).check(matches(isDisplayed()))
    }

    @Test
    fun `should include the date in the title`() {
        mockDailyActivity(enrolments = 2)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.dashboard_daily_activity_card_title)).check(
            matches(
                withText(
                    containsString(DATE),
                ),
            ),
        )
    }

    @Test
    fun `should hide the enrolment when the enrolment count is 0`() {
        mockDailyActivity(enrolments = 0, identifications = 2)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.group_enrolments)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should display the enrolment when the enrolment count is not 0`() {
        mockDailyActivity(enrolments = 2, identifications = 2)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.group_enrolments)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.dashboard_daily_activity_card_enrolments_count)).check(matches(withText("2")))
    }

    @Test
    fun `should hide the identification when the identification count is 0`() {
        mockDailyActivity(identifications = 0, enrolments = 2)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.group_identifications)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should display the identification when the identification count is not 0`() {
        mockDailyActivity(identifications = 2, enrolments = 2)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.group_identifications)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.dashboard_daily_activity_card_identifications_count)).check(
            matches(
                withText("2"),
            ),
        )
    }

    @Test
    fun `should hide the verification when the verification count is 0`() {
        mockDailyActivity(verifications = 0, enrolments = 2)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.group_verifications)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should display the verification when the verification count is not 0`() {
        mockDailyActivity(verifications = 2, enrolments = 2)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.group_verifications)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.dashboard_daily_activity_card_verifications_count)).check(
            matches(
                withText("2"),
            ),
        )
    }

    @Test
    fun `should hide both dividers if only the enrolment is not 0`() {
        mockDailyActivity(enrolments = 2)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.divider_enrolments)).check(matches(not(isDisplayed())))
        onView(withId(R.id.divider_identifications)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should hide both dividers if only the identification is not 0`() {
        mockDailyActivity(identifications = 2)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.divider_enrolments)).check(matches(not(isDisplayed())))
        onView(withId(R.id.divider_identifications)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should hide both dividers if only the verification is not 0`() {
        mockDailyActivity(verifications = 2)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.divider_enrolments)).check(matches(not(isDisplayed())))
        onView(withId(R.id.divider_identifications)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should display a divider if the enrolment and identification are not 0`() {
        mockDailyActivity(enrolments = 2, identifications = 3)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.divider_enrolments)).check(matches(isDisplayed()))
        onView(withId(R.id.divider_identifications)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should display a divider if the enrolment and verification are not 0`() {
        mockDailyActivity(enrolments = 2, verifications = 3)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.divider_enrolments)).check(matches(isDisplayed()))
        onView(withId(R.id.divider_identifications)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should display a divider if the identification and verification are not 0`() {
        mockDailyActivity(identifications = 2, verifications = 3)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.divider_enrolments)).check(matches(not(isDisplayed())))
        onView(withId(R.id.divider_identifications)).check(matches(isDisplayed()))
    }

    @Test
    fun `should display both dividers if the enrolment, identification and verification are not 0`() {
        mockDailyActivity(enrolments = 1, identifications = 2, verifications = 3)

        launchFragmentInHiltContainer<DailyActivityFragment>()

        onView(withId(R.id.divider_enrolments)).check(matches(isDisplayed()))
        onView(withId(R.id.divider_identifications)).check(matches(isDisplayed()))
    }

    private fun mockDailyActivity(
        enrolments: Int = 0,
        identifications: Int = 0,
        verifications: Int = 0,
    ) {
        every { viewModel.dailyActivity } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<DashboardDailyActivityState>>().onChanged(
                    DashboardDailyActivityState(enrolments, identifications, verifications),
                )
            }
        }
    }
}
