package com.simprints.feature.dashboard.settings.fingerselection

import android.view.View
import androidx.lifecycle.Observer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.tools.launchFragmentInHiltContainer
import com.simprints.infra.config.domain.models.Finger
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
                FingerSelectionItem(Finger.LEFT_THUMB, 1),
                FingerSelectionItem(Finger.RIGHT_THUMB, 1),
                FingerSelectionItem(Finger.LEFT_INDEX_FINGER, 2)
            )
            every { observe(any(), any()) } answers {
                secondArg<Observer<List<FingerSelectionItem>>>().onChanged(
                    listOf(
                        FingerSelectionItem(Finger.LEFT_THUMB, 1),
                        FingerSelectionItem(Finger.RIGHT_THUMB, 1),
                        FingerSelectionItem(Finger.LEFT_INDEX_FINGER, 2)
                    )
                )
            }
        }
    }

    @Test
    fun `should display the fingers correctly`() {
        mockFingerSelections(
            listOf(
                FingerSelectionItem(Finger.LEFT_THUMB, 1),
                FingerSelectionItem(Finger.RIGHT_THUMB, 3),
                FingerSelectionItem(Finger.LEFT_INDEX_FINGER, 2)
            )
        )
        launchFragmentInHiltContainer<FingerSelectionFragment>()

        onView(withId(R.id.fingerSelectionRecyclerView)).check(matches(hasChildCount(3)))

        onView(nThFingerSelection(0))
            .check(matches(hasDescendant(withText(IDR.string.l_1_finger_name))))
            .check(matches(hasDescendant(withText("1"))))

        onView(nThFingerSelection(1))
            .check(matches(hasDescendant(withText(IDR.string.r_1_finger_name))))
            .check(matches(hasDescendant(withText("3"))))

        onView(nThFingerSelection(2))
            .check(matches(hasDescendant(withText(IDR.string.l_2_finger_name))))
            .check(matches(hasDescendant(withText("2"))))
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
}
