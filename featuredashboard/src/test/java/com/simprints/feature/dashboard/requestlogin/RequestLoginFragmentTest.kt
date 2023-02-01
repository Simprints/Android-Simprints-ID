package com.simprints.feature.dashboard.requestlogin

import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.tools.di.FakeCoreModule
import com.simprints.feature.dashboard.tools.launchFragmentInHiltContainer
import com.simprints.feature.dashboard.tools.moveToState
import com.simprints.feature.dashboard.tools.testNavController
import com.simprints.infra.login.LoginManager
import dagger.hilt.android.testing.*
import io.mockk.every
import org.hamcrest.core.StringContains.containsString
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class RequestLoginFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var loginManager: LoginManager

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun `should add the text correctly`() {
        launchFragmentInHiltContainer<RequestLoginFragment>(initialState = Lifecycle.State.STARTED)
        onView(withId(R.id.tv_device_id)).check(matches(withText(containsString(FakeCoreModule.DEVICE_ID))))
        onView(withId(R.id.simprintsIdVersionTextView)).check(
            matches(
                withText(
                    containsString(
                        FakeCoreModule.PACKAGE_VERSION_NAME
                    )
                )
            )
        )
    }

    @Test
    fun `should not redirect to the main fragment if the user is not logged in when resuming the fragment`() {
        every { loginManager.signedInProjectId } returns ""

        val navController =
            testNavController(R.navigation.graph_dashboard, R.id.requestLoginFragment)

        launchFragmentInHiltContainer<RequestLoginFragment>(
            initialState = Lifecycle.State.STARTED,
            navController = navController
        ) {
            activity?.moveToState(Lifecycle.State.RESUMED)
            assertThat(navController.currentDestination?.id).isEqualTo(R.id.requestLoginFragment)
        }
    }

    @Test
    fun `should redirect to the main fragment if the user is logged in when resuming the fragment`() {
        every { loginManager.signedInProjectId } returns "project"
        every { loginManager.signedInUserId } returns "user"

        val navController =
            testNavController(R.navigation.graph_dashboard, R.id.requestLoginFragment)

        launchFragmentInHiltContainer<RequestLoginFragment>(
            initialState = Lifecycle.State.STARTED,
            navController = navController
        ) {
            activity?.moveToState(Lifecycle.State.RESUMED)

            assertThat(navController.currentDestination?.id).isEqualTo(R.id.mainFragment)
        }
    }

}
