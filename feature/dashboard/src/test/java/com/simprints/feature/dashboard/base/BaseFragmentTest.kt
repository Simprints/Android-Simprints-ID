package com.simprints.feature.dashboard.base

import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.dashboard.R
import com.simprints.infra.authstore.AuthStore
import com.simprints.testtools.hilt.launchFragmentInHiltContainer
import com.simprints.testtools.hilt.moveToState
import com.simprints.testtools.hilt.testNavController
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
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
    lateinit var authStore: AuthStore

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun `should redirect to the request login fragment if the user is not logged in`() {
        every { authStore.signedInProjectId } returns ""

        val navController = testNavController(R.navigation.graph_dashboard)

        launchFragmentInHiltContainer<BaseFragment>(
            initialState = Lifecycle.State.STARTED,
            navController = navController,
        ) {
            activity?.moveToState(Lifecycle.State.RESUMED)

            assertThat(navController.currentDestination?.id).isEqualTo(R.id.requestLoginFragment)
        }
    }

    @Test
    fun `should redirect to the main fragment if the user is logged in`() {
        every { authStore.signedInProjectId } returns "project"

        val navController = testNavController(R.navigation.graph_dashboard)

        launchFragmentInHiltContainer<BaseFragment>(
            initialState = Lifecycle.State.STARTED,
            navController = navController,
        ) {
            activity?.moveToState(Lifecycle.State.RESUMED)

            assertThat(navController.currentDestination?.id).isEqualTo(R.id.mainFragment)
        }
    }
}
