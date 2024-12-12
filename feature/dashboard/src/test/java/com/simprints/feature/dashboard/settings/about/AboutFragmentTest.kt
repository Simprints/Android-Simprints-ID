package com.simprints.feature.dashboard.settings.about

import androidx.annotation.IdRes
import androidx.lifecycle.Observer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.PreferenceMatchers.*
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.core.DeviceID
import com.simprints.core.PackageVersionName
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.feature.dashboard.R
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.testtools.hilt.launchFragmentInHiltContainer
import com.simprints.testtools.hilt.testNavController
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class AboutFragmentTest {
    companion object {
        private const val SCANNER_VERSION = "SP424242"
        private const val SYNC = "PROJECT"
        private const val SEARCH = "MODULE"
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    @DeviceID
    lateinit var deviceId: String

    @Inject
    @PackageVersionName
    lateinit var packageVersionName: String

    @BindValue
    @JvmField
    internal val viewModel = mockk<AboutViewModel>(relaxed = true) {
        every { recentUserActivity } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<RecentUserActivity>>().onChanged(
                    RecentUserActivity(SCANNER_VERSION, "", "".asTokenizableEncrypted(), 0, 0, 0, 0),
                )
            }
        }
        every { syncAndSearchConfig } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<SyncAndSearchConfig>>().onChanged(
                    SyncAndSearchConfig(SYNC, SEARCH),
                )
            }
        }
    }

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun `should hide the fingerprint preference if the modalities doesn't contain Fingerprint`() {
        mockModalities(listOf(GeneralConfiguration.Modality.FACE))

        launchFragmentInHiltContainer<AboutFragment>()

        onView(withText(IDR.string.dashboard_preference_scanner_version_title)).check(doesNotExist())
        onView(withText(SCANNER_VERSION)).check(doesNotExist())
    }

    @Test
    fun `should display the fingerprint preference if the modalities contains Fingerprint`() {
        mockModalities(listOf(GeneralConfiguration.Modality.FINGERPRINT))

        launchFragmentInHiltContainer<AboutFragment>()

        onView(withText(IDR.string.dashboard_preference_scanner_version_title)).check(matches(isDisplayed()))
        onView(withText(SCANNER_VERSION)).check(matches(isDisplayed()))
    }

    @Test
    fun `should display the toolbar`() {
        mockModalities(listOf(GeneralConfiguration.Modality.FINGERPRINT))

        launchFragmentInHiltContainer<AboutFragment>()

        onView(withId(R.id.settingsAboutToolbar)).check(matches(isDisplayed()))
    }

    @Test
    fun `should init the preferences correctly`() {
        mockModalities(listOf(GeneralConfiguration.Modality.FACE))

        launchFragmentInHiltContainer<AboutFragment>()

        onView(withText(deviceId)).check(matches(isDisplayed()))
        onView(withText(packageVersionName)).check(matches(isDisplayed()))
        onView(withText("Project Sync - Module Search")).check(matches(isDisplayed()))
    }

    @Test
    fun `should process logout when no password and clicking on logout`() {
        mockSettingsPassword(SettingsPasswordConfig.NotSet)
        mockModalities(listOf(GeneralConfiguration.Modality.FACE))
        val navController = testNavController(R.navigation.graph_dashboard, R.id.aboutFragment)

        launchFragmentInHiltContainer<AboutFragment>(navController = navController)
        onView(withText(IDR.string.dashboard_preference_logout_title)).perform(click())
        onView(withId(android.R.id.button1))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        verify(exactly = 1) { viewModel.processLogoutRequest() }
    }

    @Test
    fun `should navigate to logout sync screen when LogoutDestination_LogoutDataSyncScreen is received`() {
        runNavigationTest(
            destination = LogoutDestination.LogoutDataSyncScreen,
            targetDestinationId = R.id.logOutSyncFragment,
        )
    }

    @Test
    fun `should navigate to request login screen when LogoutDestination_LoginScreen is received`() {
        runNavigationTest(
            destination = LogoutDestination.LoginScreen,
            targetDestinationId = R.id.requestLoginFragment,
        )
    }

    private fun runNavigationTest(
        destination: LogoutDestination,
        @IdRes targetDestinationId: Int,
    ) {
        mockSettingsPassword(SettingsPasswordConfig.NotSet)
        mockModalities(listOf(GeneralConfiguration.Modality.FACE))
        mockLogoutDestination(destination)
        val navController = testNavController(R.navigation.graph_dashboard, R.id.aboutFragment)

        launchFragmentInHiltContainer<AboutFragment>(navController = navController)
        Truth.assertThat(navController.currentDestination?.id).isEqualTo(targetDestinationId)
    }

    @Test
    fun `should not logout when no password and refusing on the alert dialog`() {
        mockSettingsPassword(SettingsPasswordConfig.NotSet)
        mockModalities(listOf(GeneralConfiguration.Modality.FACE))
        val navController = testNavController(R.navigation.graph_dashboard, R.id.aboutFragment)

        launchFragmentInHiltContainer<AboutFragment>(navController = navController)
        onView(withText(IDR.string.dashboard_preference_logout_title)).perform(click())
        onView(withId(android.R.id.button2))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        Truth.assertThat(navController.currentDestination?.id).isEqualTo(R.id.aboutFragment)
        verify(exactly = 0) { viewModel.processLogoutRequest() }
    }

    @Test
    fun `should prompt password input when has password and clicking on logout`() {
        mockSettingsPassword(SettingsPasswordConfig.Locked("1234"))
        mockModalities(listOf(GeneralConfiguration.Modality.FACE))
        val navController = testNavController(R.navigation.graph_dashboard, R.id.aboutFragment)

        launchFragmentInHiltContainer<AboutFragment>(navController = navController)
        onView(withText(IDR.string.dashboard_preference_logout_title)).perform(click())
        onView(withId(R.id.password_input_field))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(replaceText("1234"))

        verify(exactly = 1) { viewModel.processLogoutRequest() }
    }

    @Test
    fun `should not log out when prompted password and it was incorrect`() {
        mockSettingsPassword(SettingsPasswordConfig.Locked("1234"))
        mockModalities(listOf(GeneralConfiguration.Modality.FACE))
        val navController = testNavController(R.navigation.graph_dashboard, R.id.aboutFragment)

        launchFragmentInHiltContainer<AboutFragment>(navController = navController)
        onView(withText(IDR.string.dashboard_preference_logout_title)).perform(click())
        onView(withId(R.id.password_input_field))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(replaceText("1111"))

        Truth.assertThat(navController.currentDestination?.id).isEqualTo(R.id.aboutFragment)
        verify(exactly = 0) { viewModel.processLogoutRequest() }
    }

    private fun mockModalities(modalities: List<GeneralConfiguration.Modality>) {
        every { viewModel.modalities } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<List<GeneralConfiguration.Modality>>>().onChanged(modalities)
            }
        }
    }

    private fun mockSettingsPassword(lock: SettingsPasswordConfig) {
        every { viewModel.settingsLocked } returns mockk {
            every { value } returns lock
        }
    }

    private fun mockLogoutDestination(destination: LogoutDestination) {
        every { viewModel.logoutDestinationEvent } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<LiveDataEventWithContent<LogoutDestination>>>().onChanged(
                    LiveDataEventWithContent(destination),
                )
            }
        }
    }
}
