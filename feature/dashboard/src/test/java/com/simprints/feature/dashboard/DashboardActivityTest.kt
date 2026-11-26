package com.simprints.feature.dashboard

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.os.Looper
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.broadcasts.InternalBroadcaster
import com.simprints.core.tools.utils.LanguageHelper
import com.simprints.feature.dashboard.requestlogin.LogoutReason
import com.simprints.feature.dashboard.requestlogin.RequestLoginFragmentArgs
import com.simprints.feature.storage.alert.ShowStorageAlertIfNecessaryUseCase
import com.simprints.infra.authstore.AuthStore
import com.simprints.testtools.hilt.testNavController
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

/**
 * Pins the terminal step of every logout flow: the logout broadcast received by [DashboardActivity] routes to `requestLoginFragment`.
 *
 * The broadcast is sent with the real [InternalBroadcaster], so the action and extra names are pinned too.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class DashboardActivityTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    val showStorageAlertIfNecessary = mockk<ShowStorageAlertIfNecessaryUseCase>(relaxed = true)

    @Inject
    lateinit var authStore: AuthStore

    private val context: Context = ApplicationProvider.getApplicationContext()

    private lateinit var scenario: ActivityScenario<DashboardActivity>

    @Before
    fun setUp() {
        hiltRule.inject()
        LanguageHelper.init(context)
        applyAppTheme()
        grantNotExportedReceiverPermission()
        // Signed out: the real NavHostFragment settles on requestLoginFragment after BaseFragment's redirect.
        every { authStore.signedInProjectId } returns ""
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
    }

    @Test
    fun `logout broadcast with project ended navigates from main to request login with the project-ending reason`() {
        scenario = ActivityScenario.launch(DashboardActivity::class.java)
        val navController = attachTestNavController(startDestination = R.id.mainFragment)

        sendLogoutBroadcast(isProjectEnded = true)

        assertThat(navController.currentDestination?.id).isEqualTo(R.id.requestLoginFragment)
        assertThat(navController.backStack.map { it.destination.id })
            .containsExactly(R.id.dashboard_navigation, R.id.requestLoginFragment)
            .inOrder()
        assertThat(navController.currentLogoutReason()).isEqualTo(
            LogoutReason(
                title = context.getString(IDR.string.dashboard_sync_project_ending_alert_title),
                body = context.getString(IDR.string.dashboard_sync_project_ending_message),
            ),
        )
    }

    @Test
    fun `logout broadcast without project ended navigates from main to request login without a reason`() {
        scenario = ActivityScenario.launch(DashboardActivity::class.java)
        val navController = attachTestNavController(startDestination = R.id.mainFragment)

        sendLogoutBroadcast(isProjectEnded = false)

        assertThat(navController.currentDestination?.id).isEqualTo(R.id.requestLoginFragment)
        assertThat(navController.backStack.map { it.destination.id })
            .containsExactly(R.id.dashboard_navigation, R.id.requestLoginFragment)
            .inOrder()
        assertThat(navController.currentLogoutReason()).isNull()
    }

    @Test
    fun `second logout broadcast while on request login does not crash and keeps the destination`() {
        // Uses the real NavHostFragment, so each navigation is a real fragment transaction.
        scenario = ActivityScenario.launch(DashboardActivity::class.java)
        assertThat(realNavController().currentDestination?.id).isEqualTo(R.id.requestLoginFragment)
        assertThat(realNavController().currentLogoutReason()).isNull()

        // The first broadcast is proven delivered by the reason it leaves in the arguments.
        sendLogoutBroadcast(isProjectEnded = true)
        assertThat(realNavController().currentDestination?.id).isEqualTo(R.id.requestLoginFragment)
        assertThat(realNavController().currentLogoutReason()).isNotNull()

        // The second broadcast is proven delivered by clearing the reason again.
        sendLogoutBroadcast(isProjectEnded = false)

        assertThat(scenario.state).isEqualTo(Lifecycle.State.RESUMED)
        val navController = realNavController()
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.requestLoginFragment)
        assertThat(navController.currentLogoutReason()).isNull()
        assertThat(navController.previousBackStackEntry).isNull()
    }

    /**
     * The library manifest declares no theme; in production `:id` sets `Theme.Simprints` on the application.
     * Robolectric does not merge the app manifest, so give the activity the same theme here.
     */
    private fun applyAppTheme() {
        val component = ComponentName(context, DashboardActivity::class.java)
        val activityInfo = context.packageManager.getActivityInfo(component, 0)
        activityInfo.theme = IDR.style.Theme_Simprints
        shadowOf(context.packageManager).addOrUpdateActivity(activityInfo)
    }

    /**
     * Below API 33 `ContextCompat.registerReceiver(..., RECEIVER_NOT_EXPORTED)` (used by `BaseActivity.onStart`) needs the
     * app-private permission that androidx.core adds to the app's merged manifest. Grant it as the installed app has it.
     */
    private fun grantNotExportedReceiverPermission() {
        shadowOf(context as Application).grantPermissions("${context.packageName}.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION")
    }

    /**
     * Replaces the controller that `findNavController(R.id.nav_host_fragment)` resolves inside the activity,
     * so the logout can start from a destination other than the one the signed-out redirect lands on.
     */
    private fun attachTestNavController(startDestination: Int): TestNavHostController {
        lateinit var navController: TestNavHostController
        scenario.onActivity { activity ->
            navController = testNavController(R.navigation.graph_dashboard, startDestination)
            Navigation.setViewNavController(activity.findViewById<View>(R.id.nav_host_fragment), navController)
        }
        assertThat(navController.currentDestination?.id).isEqualTo(startDestination)
        return navController
    }

    private fun realNavController(): NavController {
        lateinit var navController: NavController
        scenario.onActivity { activity -> navController = activity.findNavController(R.id.nav_host_fragment) }
        return navController
    }

    private fun sendLogoutBroadcast(isProjectEnded: Boolean) {
        InternalBroadcaster(context).loggedOut(isProjectEnded)
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun NavController.currentLogoutReason(): LogoutReason? = RequestLoginFragmentArgs
        .fromBundle(checkNotNull(currentBackStackEntry?.arguments))
        .logoutReason
}
