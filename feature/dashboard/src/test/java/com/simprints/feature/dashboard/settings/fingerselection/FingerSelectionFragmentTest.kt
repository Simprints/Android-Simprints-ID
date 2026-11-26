package com.simprints.feature.dashboard.settings.fingerselection

import android.os.Looper
import android.view.View
import androidx.fragment.app.commitNow
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.TemplateIdentifier
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.tools.MviStubs
import com.simprints.feature.dashboard.tools.stubMvi
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.testtools.hilt.launchFragmentInHiltContainer
import com.simprints.testtools.hilt.testNavController
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import com.simprints.infra.resources.R as IDR

private const val SIM_MATCHER_NAME = "SimMatcher"
private const val NEC_NAME = "NEC"

private val TWO_SECTIONS = listOf(
    FingerSelectionSection(
        sdkName = SIM_MATCHER_NAME,
        items = listOf(
            FingerSelectionItem(TemplateIdentifier.LEFT_THUMB, 1),
            FingerSelectionItem(TemplateIdentifier.RIGHT_THUMB, 2),
        ),
    ),
    FingerSelectionSection(
        sdkName = NEC_NAME,
        items = listOf(FingerSelectionItem(TemplateIdentifier.LEFT_INDEX_FINGER, 3)),
    ),
)

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class FingerSelectionFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val viewModel = mockk<FingerSelectionViewModel>(relaxed = true)

    private lateinit var mvi: MviStubs<FingerSelectionUiState, FingerSelectionUiEffect>

    @Before
    fun setUp() {
        mvi = viewModel.stubMvi(FingerSelectionUiState())
    }

    @Test
    fun `should display the fingers correctly`() {
        mvi.state.value = FingerSelectionUiState(
            sections = listOf(
                FingerSelectionSection(
                    sdkName = SIM_MATCHER_NAME,
                    items = listOf(
                        FingerSelectionItem(TemplateIdentifier.LEFT_THUMB, 1),
                        FingerSelectionItem(TemplateIdentifier.RIGHT_THUMB, 2),
                        FingerSelectionItem(TemplateIdentifier.LEFT_INDEX_FINGER, 3),
                    ),
                ),
            ),
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

    @Test
    fun `two sections render header and rows in order`() {
        mvi.state.value = FingerSelectionUiState(sections = TWO_SECTIONS)
        launchFragmentInHiltContainer<FingerSelectionFragment>()

        assertTwoSectionsRendered()
    }

    @Test
    fun `view re-creation loads again and renders a single copy of the sections`() {
        mvi.state.value = FingerSelectionUiState(sections = TWO_SECTIONS)

        launchFragmentInHiltContainer<FingerSelectionFragment> {
            verify(exactly = 1) { viewModel.onAction(FingerSelectionUiAction.Load) }

            // Destroys and re-creates the view while the Fragment (and its ViewModel) stays, as when returning from the back stack.
            parentFragmentManager.commitNow { detach(this@launchFragmentInHiltContainer) }
            parentFragmentManager.commitNow { attach(this@launchFragmentInHiltContainer) }
        }

        verify(exactly = 2) { viewModel.onAction(FingerSelectionUiAction.Load) }
        assertTwoSectionsRendered()

        // A later state replaces the rows instead of adding to them.
        mvi.state.value = FingerSelectionUiState(
            sections = listOf(
                FingerSelectionSection(
                    sdkName = NEC_NAME,
                    items = listOf(FingerSelectionItem(TemplateIdentifier.RIGHT_INDEX_FINGER, 4)),
                ),
            ),
        )
        onView(withId(R.id.fingerSelectionRecyclerView)).check(matches(hasChildCount(2)))
        onView(nThFingerSelection(0)).check(matches(hasDescendant(withText(NEC_NAME))))
        onView(nThFingerSelection(1))
            .check(matches(hasDescendant(withText(IDR.string.fingerprint_capture_finger_r_2))))
            .check(matches(hasDescendant(withText("4"))))
    }

    @Test
    fun `a configuration with an unknown finger renders without crashing`() {
        val fingerprintConfiguration = mockk<FingerprintConfiguration> {
            every { secugenSimMatcher?.fingersToCapture } returns listOf(
                TemplateIdentifier.LEFT_THUMB,
                TemplateIdentifier.NONE,
                TemplateIdentifier.LEFT_THUMB,
            )
            every { nec } returns null
        }
        val configRepository = mockk<ConfigRepository> {
            coEvery { getProjectConfiguration().fingerprint } returns fingerprintConfiguration
        }
        val realViewModel = FingerSelectionViewModel(configRepository)
        every { viewModel.uiState } returns realViewModel.uiState
        every { viewModel.onAction(any()) } answers { realViewModel.onAction(firstArg()) }

        launchFragmentInHiltContainer<FingerSelectionFragment>()
        shadowOf(Looper.getMainLooper()).idle()

        onView(withId(R.id.fingerSelectionRecyclerView)).check(matches(hasChildCount(2)))
        onView(nThFingerSelection(0)).check(matches(hasDescendant(withText(SIM_MATCHER_NAME))))
        onView(nThFingerSelection(1))
            .check(matches(hasDescendant(withText(IDR.string.fingerprint_capture_finger_l_1))))
            .check(matches(hasDescendant(withText("2"))))
    }

    @Test
    fun `toolbar up navigates back to settings`() {
        val navController = testNavController(R.navigation.graph_dashboard, R.id.settingsFragment)
        navController.navigate(R.id.action_settingsFragment_to_fingerSelectionFragment)
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.fingerSelectionFragment)

        launchFragmentInHiltContainer<FingerSelectionFragment>(navController = navController)
        onView(withContentDescription("back")).perform(click())

        assertThat(navController.currentDestination?.id).isEqualTo(R.id.settingsFragment)
    }

    private fun assertTwoSectionsRendered() {
        onView(withId(R.id.fingerSelectionRecyclerView)).check(matches(hasChildCount(5)))
        onView(nThFingerSelection(0)).check(matches(hasDescendant(withText(SIM_MATCHER_NAME))))
        onView(nThFingerSelection(1))
            .check(matches(hasDescendant(withText(IDR.string.fingerprint_capture_finger_l_1))))
            .check(matches(hasDescendant(withText("1"))))
        onView(nThFingerSelection(2))
            .check(matches(hasDescendant(withText(IDR.string.fingerprint_capture_finger_r_1))))
            .check(matches(hasDescendant(withText("2"))))
        onView(nThFingerSelection(3)).check(matches(hasDescendant(withText(NEC_NAME))))
        onView(nThFingerSelection(4))
            .check(matches(hasDescendant(withText(IDR.string.fingerprint_capture_finger_l_2))))
            .check(matches(hasDescendant(withText("3"))))
    }

    private fun nThFingerSelection(position: Int): Matcher<View> = allOf(
        withParent(withId(R.id.fingerSelectionRecyclerView)),
        withParentIndex(position),
    )
}
