package com.simprints.feature.dashboard.settings.fingerselection

import android.view.View
import androidx.lifecycle.Observer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.feature.dashboard.R
import com.simprints.testtools.hilt.launchFragmentInHiltContainer
import com.simprints.infra.config.store.models.Finger
import com.simprints.infra.resources.R as IDR
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

private const val SIM_MATCHER_NAME = "SimMatcher"

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class FingerSelectionFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val viewModel = mockk<FingerSelectionViewModel>(relaxed = true)

    @Test
    fun `should display the fingers correctly`() {
        mockFingerSelections(
            listOf(
                FingerSelectionSection(
                    sdkName = SIM_MATCHER_NAME,
                    items = listOf(
                        FingerSelectionItem(Finger.LEFT_THUMB, 1),
                        FingerSelectionItem(Finger.RIGHT_THUMB, 2),
                        FingerSelectionItem(Finger.LEFT_INDEX_FINGER, 3)
                    )
                ),
            )
        )
        launchFragmentInHiltContainer<FingerSelectionFragment>()

        onView(withId(R.id.fingerSelectionRecyclerView)).check(matches(hasChildCount(4)))

        onView(nThFingerSelection(0))
            .check(matches(hasDescendant(withText(SIM_MATCHER_NAME))))

        onView(nThFingerSelection(1))
            .check(matches(hasDescendant(withText(IDR.string.fingerprint_capture_finger_l_1))))
            .check(matches(hasDescendant(withText("1"))))

        onView(nThFingerSelection(2))
            .check(matches(hasDescendant(withText(IDR.string.fingerprint_capture_finger_r_1))))
            .check(matches(hasDescendant(withText("2"))))

        onView(nThFingerSelection(3))
            .check(matches(hasDescendant(withText(IDR.string.fingerprint_capture_finger_l_2))))
            .check(matches(hasDescendant(withText("3"))))
    }

    private fun mockFingerSelections(fingers: List<FingerSelectionSection>) {
        every { viewModel.fingerSelections } returns mockk {
            every { value } returns fingers
            every { observe(any(), any()) } answers {
                secondArg<Observer<List<FingerSelectionSection>>>().onChanged(fingers)
            }
        }
    }

    private fun nThFingerSelection(position: Int): Matcher<View> {
        return allOf(
            withParent(withId(R.id.fingerSelectionRecyclerView)),
            withParentIndex(position)
        )
    }
}
