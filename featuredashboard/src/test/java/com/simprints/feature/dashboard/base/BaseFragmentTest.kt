package com.simprints.feature.dashboard.base

import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.tools.FRAGMENT_TAG
import com.simprints.feature.dashboard.tools.launchFragmentInHiltContainer
import com.simprints.feature.dashboard.tools.moveToState
import com.simprints.infra.login.LoginManager
import dagger.hilt.android.testing.*
import io.mockk.every
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class BaseFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var loginManager: LoginManager

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun `should redirect to the request login fragment if the user is not logged in`() {
        every { loginManager.signedInProjectId } returns ""
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.setGraph(R.navigation.graph_dashboard)

        launchFragmentInHiltContainer<BaseFragment>(initialState = Lifecycle.State.STARTED) {
            val fragment = activity?.supportFragmentManager?.findFragmentByTag(FRAGMENT_TAG)!!
            Navigation.setViewNavController(fragment.requireView(), navController)
            activity?.moveToState(Lifecycle.State.RESUMED)

            assertThat(navController.currentDestination?.id).isEqualTo(R.id.requestLoginFragment)
        }
    }

    @Test
    fun `should redirect to the main fragment if the user is logged in`() {
        every { loginManager.signedInProjectId } returns "project"
        every { loginManager.signedInUserId } returns "user"

        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.setGraph(R.navigation.graph_dashboard)

        launchFragmentInHiltContainer<BaseFragment>(initialState = Lifecycle.State.STARTED) {
            val fragment = activity?.supportFragmentManager?.findFragmentByTag(FRAGMENT_TAG)!!
            Navigation.setViewNavController(fragment.requireView(), navController)
            activity?.moveToState(Lifecycle.State.RESUMED)

            assertThat(navController.currentDestination?.id).isEqualTo(R.id.mainFragment)
        }
    }
}
