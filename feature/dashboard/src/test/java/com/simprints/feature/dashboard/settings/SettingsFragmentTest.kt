package com.simprints.feature.dashboard.settings

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.PreferenceMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.dashboard.R
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.testtools.hilt.launchFragmentInHiltContainer
import com.simprints.testtools.hilt.testNavController
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import com.simprints.infra.resources.R as IDR

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class SettingsFragmentTest {
    companion object {
        private val LANGUAGE_OPTIONS = listOf("en", "fr", "pt")
        private const val LANGUAGE = "en"
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private val configuration = mockk<GeneralConfiguration> {
        every { languageOptions } returns LANGUAGE_OPTIONS
    }

    @BindValue
    @JvmField
    internal val viewModel = mockk<SettingsViewModel>(relaxed = true) {
        every { generalConfiguration } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<GeneralConfiguration>>().onChanged(configuration)
            }
        }
        every { languagePreference } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<String>>().onChanged(LANGUAGE)
            }
        }
    }

    @Test
    fun `should display the toolbar`() {
        mockModalities(listOf(GeneralConfiguration.Modality.FINGERPRINT))

        launchFragmentInHiltContainer<SettingsFragment>()

        onView(withId(R.id.settingsToolbar)).check(matches(isDisplayed()))
    }

    @Test
    fun `should hide the fingerprint preference if the modalities doesn't contain Fingerprint`() {
        mockModalities(listOf(GeneralConfiguration.Modality.FACE))

        launchFragmentInHiltContainer<SettingsFragment>()

        onView(withText(IDR.string.dashboard_preference_view_fingers_title)).check(doesNotExist())
    }

    @Test
    fun `should display the fingerprint preference if the modalities contains Fingerprint`() {
        mockModalities(listOf(GeneralConfiguration.Modality.FINGERPRINT))

        launchFragmentInHiltContainer<SettingsFragment>()

        onView(withText(IDR.string.dashboard_preference_view_fingers_title)).check(matches(isDisplayed()))
    }

    @Test
    fun `should redirect to the sync info fragment when clicking on the sync info preference`() {
        mockModalities(listOf(GeneralConfiguration.Modality.FINGERPRINT))

        val navController = testNavController(R.navigation.graph_dashboard, R.id.settingsFragment)

        launchFragmentInHiltContainer<SettingsFragment>(navController = navController)

        onView(withText(IDR.string.dashboard_preference_sync_information_title)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.syncInfoFragment)
    }

    @Test
    fun `should redirect to the finger selection fragment when clicking on the finger selection preference`() {
        mockModalities(listOf(GeneralConfiguration.Modality.FINGERPRINT))

        val navController = testNavController(R.navigation.graph_dashboard, R.id.settingsFragment)

        launchFragmentInHiltContainer<SettingsFragment>(navController = navController)

        onView(withText(IDR.string.dashboard_preference_view_fingers_title)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.fingerSelectionFragment)
    }

    @Test
    fun `should redirect to the about fragment when clicking on the about preference`() {
        mockModalities(listOf(GeneralConfiguration.Modality.FACE))

        val navController = testNavController(R.navigation.graph_dashboard, R.id.settingsFragment)

        launchFragmentInHiltContainer<SettingsFragment>(navController = navController)

        onView(withId(androidx.preference.R.id.recycler_view)).perform(
            RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText(IDR.string.dashboard_preference_app_details_title)),
            ),
        )
        onView(withText(IDR.string.dashboard_preference_app_details_title)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.aboutFragment)
    }

    @Test
    fun `should add the selected language as the summary of the language preference`() {
        mockModalities(listOf(GeneralConfiguration.Modality.FACE))

        launchFragmentInHiltContainer<SettingsFragment>()

        onView(withText("English")).check(matches(isDisplayed()))
    }

    private fun mockModalities(modalities: List<GeneralConfiguration.Modality>) {
        every { configuration.modalities } returns modalities
    }
}
