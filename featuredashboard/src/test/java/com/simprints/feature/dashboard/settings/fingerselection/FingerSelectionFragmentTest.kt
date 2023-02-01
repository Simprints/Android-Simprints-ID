package com.simprints.feature.dashboard.settings.fingerselection

import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.tools.launchFragmentInHiltContainer
import com.simprints.infra.config.domain.models.Finger
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf.allOf
import org.hamcrest.core.IsNot.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class FingerSelectionFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val viewModel = mockk<FingerSelectionViewModel>(relaxed = true) {
        every { fingerSelections } returns mockk {
            every { value } returns listOf(
                FingerSelectionItem(Finger.LEFT_THUMB, 1, false),
                FingerSelectionItem(Finger.RIGHT_THUMB, 1, false),
                FingerSelectionItem(Finger.LEFT_INDEX_FINGER, 2, true)
            )
            every { observe(any(), any()) } answers {
                secondArg<Observer<List<FingerSelectionItem>>>().onChanged(
                    listOf(
                        FingerSelectionItem(Finger.LEFT_THUMB, 1, false),
                        FingerSelectionItem(Finger.RIGHT_THUMB, 1, false),
                        FingerSelectionItem(Finger.LEFT_INDEX_FINGER, 2, true)
                    )
                )
            }
        }
    }

    @Test
    fun `should display the fingers correctly`() {
        mockFingerSelections(
            listOf(
                FingerSelectionItem(Finger.LEFT_THUMB, 1, false),
                FingerSelectionItem(Finger.RIGHT_THUMB, 3, false),
                FingerSelectionItem(Finger.LEFT_INDEX_FINGER, 2, true)
            )
        )
        launchFragmentInHiltContainer<FingerSelectionFragment>()

        onView(withId(R.id.fingerSelectionRecyclerView)).check(matches(hasChildCount(3)))

        onView(nThFingerSelection(0))
            .check(matches(hasDescendant(withText("Left Thumb"))))
            .check(matches(hasDescendant(withText("1"))))

        onView(nThFingerSelection(1))
            .check(matches(hasDescendant(withText("Right Thumb"))))
            .check(matches(hasDescendant(withText("3"))))

        onView(nThFingerSelection(2))
            .check(matches(hasDescendant(withText("Left Index Finger"))))
            .check(matches(hasDescendant(withText("2"))))
    }

    @Test
    fun `should only enable to change the fingers for non removable ones`() {
        mockFingerSelections(
            listOf(
                FingerSelectionItem(Finger.LEFT_THUMB, 1, false),
                FingerSelectionItem(Finger.RIGHT_THUMB, 3, false),
                FingerSelectionItem(Finger.LEFT_INDEX_FINGER, 2, true)
            )
        )
        launchFragmentInHiltContainer<FingerSelectionFragment>()

        onView(withText("Left Thumb")).check(matches(not(isEnabled())))
        onView(withText("Right Thumb")).check(matches(not(isEnabled())))
        onView(withText("Left Index Finger")).check(matches(isEnabled()))
    }

    @Test
    fun `should only enable to delete the fingers for non removable ones`() {
        mockFingerSelections(
            listOf(
                FingerSelectionItem(Finger.LEFT_THUMB, 1, false),
                FingerSelectionItem(Finger.RIGHT_THUMB, 3, false),
                FingerSelectionItem(Finger.LEFT_INDEX_FINGER, 2, true)
            )
        )
        launchFragmentInHiltContainer<FingerSelectionFragment>()

        // Left thumb
        onView(nThDeleteFinger(0)).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))

        // Right thumb
        onView(nThDeleteFinger(1)).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))

        // Left index finger
        onView(nThDeleteFinger(2)).check(matches(isDisplayed()))
    }

    @Test
    fun `should be able to remove a finger correctly`() {
        mockFingerSelections(
            listOf(
                FingerSelectionItem(Finger.LEFT_THUMB, 1, false),
                FingerSelectionItem(Finger.RIGHT_THUMB, 3, false),
                FingerSelectionItem(Finger.LEFT_INDEX_FINGER, 2, true)
            )
        )
        launchFragmentInHiltContainer<FingerSelectionFragment>()

        // Left thumb
        onView(nThDeleteFinger(2)).perform(click())

        verify(exactly = 1) { viewModel.removeItem(2) }
    }

    @Test
    fun `should be able to reset the fingers correctly`() {
        mockFingerSelections(
            listOf(
                FingerSelectionItem(Finger.LEFT_THUMB, 1, false),
                FingerSelectionItem(Finger.RIGHT_THUMB, 3, false),
                FingerSelectionItem(Finger.LEFT_INDEX_FINGER, 2, true)
            )
        )
        launchFragmentInHiltContainer<FingerSelectionFragment>()

        onView(withId(R.id.resetButton)).perform(click())

        verify(exactly = 1) { viewModel.resetFingerItems() }
    }

    @Test
    fun `should be able to add a new finger correctly`() {
        mockFingerSelections(
            listOf(
                FingerSelectionItem(Finger.LEFT_THUMB, 1, false),
                FingerSelectionItem(Finger.RIGHT_THUMB, 3, false),
                FingerSelectionItem(Finger.LEFT_INDEX_FINGER, 2, true)
            )
        )
        launchFragmentInHiltContainer<FingerSelectionFragment>()

        onView(withId(R.id.addFingerButton)).perform(click())

        verify(exactly = 1) { viewModel.addNewFinger() }
    }

    @Test
    fun `should prevent to add a new finger when there is already 10 fingers`() {
        mockFingerSelections(
            listOf(
                FingerSelectionItem(Finger.LEFT_THUMB, 1, false),
                FingerSelectionItem(Finger.LEFT_INDEX_FINGER, 2, true),
                FingerSelectionItem(Finger.LEFT_3RD_FINGER, 2, true),
                FingerSelectionItem(Finger.LEFT_4TH_FINGER, 2, true),
                FingerSelectionItem(Finger.LEFT_5TH_FINGER, 2, true),
                FingerSelectionItem(Finger.RIGHT_THUMB, 1, true),
                FingerSelectionItem(Finger.RIGHT_INDEX_FINGER, 2, true),
                FingerSelectionItem(Finger.RIGHT_3RD_FINGER, 2, true),
                FingerSelectionItem(Finger.RIGHT_4TH_FINGER, 2, true),
                FingerSelectionItem(Finger.RIGHT_5TH_FINGER, 2, true),
            )
        )
        launchFragmentInHiltContainer<FingerSelectionFragment>()

        onView(withId(R.id.addFingerButton)).check(matches(not(isEnabled())))
    }

    @Test
    fun `should navigate back when clicking on the back button and nothing has changed`() {
        mockFingerSelections(listOf(FingerSelectionItem(Finger.LEFT_THUMB, 1, false)))

        val navController = mockk<NavController>(relaxed = true)

        launchFragmentInHiltContainer<FingerSelectionFragment>(navController = navController)

        onView(withId(R.id.fingerSelectionRecyclerView)).perform(pressBack())

        verify(exactly = 1) { navController.popBackStack() }
    }

    @Test
    fun `should display the save dialog when clicking on the back button and the selection has changed and save the selection if validating`() {
        every { viewModel.hasSelectionChanged() } returns true

        mockFingerSelections(listOf(FingerSelectionItem(Finger.LEFT_THUMB, 1, false)))

        val navController = mockk<NavController>(relaxed = true)

        launchFragmentInHiltContainer<FingerSelectionFragment>(navController = navController)

        onView(withId(R.id.fingerSelectionRecyclerView)).perform(pressBack())
        onView(withId(android.R.id.button1))
            .inRoot(RootMatchers.isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        verify(exactly = 1) { viewModel.savePreference() }
        verify(exactly = 1) { navController.popBackStack() }
    }

    @Test
    fun `should display the save dialog when clicking on the back button and the selection has changed and not save the selection if canceling`() {
        every { viewModel.hasSelectionChanged() } returns true

        mockFingerSelections(listOf(FingerSelectionItem(Finger.LEFT_THUMB, 1, false)))

        val navController = mockk<NavController>(relaxed = true)

        launchFragmentInHiltContainer<FingerSelectionFragment>(navController = navController)

        onView(withId(R.id.fingerSelectionRecyclerView)).perform(pressBack())
        onView(withId(android.R.id.button2))
            .inRoot(RootMatchers.isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        verify(exactly = 0) { viewModel.savePreference() }
        verify(exactly = 1) { navController.popBackStack() }
    }

    @Test
    fun `should navigate back when clicking on the back navigation and nothing has changed`() {
        mockFingerSelections(listOf(FingerSelectionItem(Finger.LEFT_THUMB, 1, false)))

        val navController = mockk<NavController>(relaxed = true)

        launchFragmentInHiltContainer<FingerSelectionFragment>(navController = navController)

        onView(withContentDescription("back")).perform(click())

        verify(exactly = 1) { navController.popBackStack() }
    }

    @Test
    fun `should display the save dialog when clicking on the back navigation and the selection has changed and save the selection if validating`() {
        every { viewModel.hasSelectionChanged() } returns true

        mockFingerSelections(listOf(FingerSelectionItem(Finger.LEFT_THUMB, 1, false)))

        val navController = mockk<NavController>(relaxed = true)

        launchFragmentInHiltContainer<FingerSelectionFragment>(navController = navController)

        onView(withContentDescription("back")).perform(click())
        onView(withId(android.R.id.button1))
            .inRoot(RootMatchers.isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        verify(exactly = 1) { viewModel.savePreference() }
        verify(exactly = 1) { navController.popBackStack() }
    }

    @Test
    fun `should display the save dialog when clicking on the back navigation and the selection has changed and not save the selection if canceling`() {
        every { viewModel.hasSelectionChanged() } returns true

        mockFingerSelections(listOf(FingerSelectionItem(Finger.LEFT_THUMB, 1, false)))

        val navController = mockk<NavController>(relaxed = true)

        launchFragmentInHiltContainer<FingerSelectionFragment>(navController = navController)

        onView(withContentDescription("back")).perform(click())
        onView(withId(android.R.id.button2))
            .inRoot(RootMatchers.isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        verify(exactly = 0) { viewModel.savePreference() }
        verify(exactly = 1) { navController.popBackStack() }
    }

    private fun mockFingerSelections(fingers: List<FingerSelectionItem>) {
        every { viewModel.fingerSelections } returns mockk {
            every { value } returns fingers
            every { observe(any(), any()) } answers {
                secondArg<Observer<List<FingerSelectionItem>>>().onChanged(fingers)
            }
        }
    }

    private fun nThFingerSelection(position: Int): Matcher<View> {
        return allOf(
            withParent(withId(R.id.fingerSelectionRecyclerView)),
            withParentIndex(position)
        )
    }

    private fun nThDeleteFinger(position: Int): Matcher<View> {
        return allOf(
            withParent(nThFingerSelection(position)),
            withId(R.id.deleteFingerSelectionImageView)
        )
    }
}
