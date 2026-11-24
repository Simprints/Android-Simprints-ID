package com.simprints.feature.dashboard.requestlogin

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.tools.di.FakeCoreModule
import com.simprints.infra.authstore.AuthStore
import com.simprints.testtools.hilt.FRAGMENT_TAG
import com.simprints.testtools.hilt.HiltTestActivity
import com.simprints.testtools.hilt.launchFragmentInHiltContainer
import com.simprints.testtools.hilt.moveToState
import com.simprints.testtools.hilt.testNavController
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import org.hamcrest.core.StringContains.containsString
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowDialog
import javax.inject.Inject
import com.google.android.material.R as MR
import com.simprints.feature.troubleshooting.R as TroubleshootingR
import com.simprints.infra.resources.R as IDR

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class RequestLoginFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var authStore: AuthStore

    private val context: Context = ApplicationProvider.getApplicationContext()

    private var scenario: ActivityScenario<HiltTestActivity>? = null

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        scenario?.close()
        scenario = null
    }

    @Test
    fun `should add the text correctly`() {
        launchFragmentInHiltContainer<RequestLoginFragment>(
            initialState = Lifecycle.State.STARTED,
            fragmentArgs = Bundle(),
        )
        onView(withId(R.id.tv_device_id)).check(matches(withText(containsString(FakeCoreModule.DEVICE_ID))))
        onView(withId(R.id.simprintsIdVersionTextView)).check(
            matches(
                withText(
                    containsString(
                        FakeCoreModule.PACKAGE_VERSION_NAME,
                    ),
                ),
            ),
        )
    }

    @Test
    fun `Should not redirect to the main fragment if the user is not logged in when resuming the fragment`() {
        every { authStore.signedInProjectId } returns ""

        val navController =
            testNavController(R.navigation.graph_dashboard, R.id.requestLoginFragment)

        launchFragmentInHiltContainer<RequestLoginFragment>(
            initialState = Lifecycle.State.STARTED,
            navController = navController,
            fragmentArgs = Bundle(),
        ) {
            activity?.moveToState(Lifecycle.State.RESUMED)
            assertThat(navController.currentDestination?.id).isEqualTo(R.id.requestLoginFragment)
        }
    }

    @Test
    fun `Should redirect to the main fragment if the user is logged in when resuming the fragment`() {
        every { authStore.signedInProjectId } returns "project"

        val navController =
            testNavController(R.navigation.graph_dashboard, R.id.requestLoginFragment)

        launchFragmentInHiltContainer<RequestLoginFragment>(
            initialState = Lifecycle.State.STARTED,
            navController = navController,
            fragmentArgs = Bundle(),
        ) {
            activity?.moveToState(Lifecycle.State.RESUMED)

            assertThat(navController.currentDestination?.id).isEqualTo(R.id.mainFragment)
        }
    }

    @Test
    fun `Logout reason argument shows a dialog with its title and body that the close button dismisses`() {
        every { authStore.signedInProjectId } returns ""

        launchFragmentInHiltContainer<RequestLoginFragment>(
            initialState = Lifecycle.State.RESUMED,
            fragmentArgs = RequestLoginFragmentArgs(logoutReason = LOGOUT_REASON).toBundle(),
        )
        // Let the dialog finish showing (its show callbacks are posted to the main looper) before interacting.
        shadowOf(Looper.getMainLooper()).idle()

        assertThat(ShadowDialog.getShownDialogs()).hasSize(1)
        val dialog = ShadowDialog.getLatestDialog() as AlertDialog
        assertThat(dialog.isShowing).isTrue()
        assertThat(dialog.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)?.text?.toString())
            .isEqualTo(LOGOUT_REASON.title)
        assertThat(dialog.findViewById<TextView>(android.R.id.message)?.text?.toString())
            .isEqualTo(LOGOUT_REASON.body)
        val closeButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        assertThat(closeButton.text.toString())
            .isEqualTo(context.getString(IDR.string.dashboard_request_login_close_reason_dialog))

        closeButton.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertThat(dialog.isShowing).isFalse()
    }

    @Test
    fun `Logout reason dialog is not shown again when the fragment is re-created with saved state`() {
        every { authStore.signedInProjectId } returns ""
        val activityScenario = launchRecreatableRequestLogin(logoutReason = LOGOUT_REASON)
        val originalFragment = activityScenario.requestLoginFragment()
        assertThat(ShadowDialog.getShownDialogs()).hasSize(1)

        activityScenario.recreate()
        shadowOf(Looper.getMainLooper()).idle()

        // The fragment and its view were re-created from saved state (same arguments, new instance, onViewCreated ran again).
        val recreatedFragment = activityScenario.requestLoginFragment()
        assertThat(recreatedFragment).isNotSameInstanceAs(originalFragment)
        assertThat(RequestLoginFragmentArgs.fromBundle(recreatedFragment.requireArguments()).logoutReason)
            .isEqualTo(LOGOUT_REASON)
        assertThat(
            recreatedFragment
                .requireView()
                .findViewById<TextView>(R.id.tv_device_id)
                .text
                .toString(),
        ).isEqualTo(context.getString(IDR.string.dashboard_request_login_device_id, FakeCoreModule.DEVICE_ID))
        // No second dialog.
        assertThat(ShadowDialog.getShownDialogs()).hasSize(1)
    }

    @Test
    fun `logout reason dialog is not shown again when the view is re-created after it was closed`() {
        every { authStore.signedInProjectId } returns ""
        lateinit var fragment: Fragment
        launchFragmentInHiltContainer<RequestLoginFragment>(
            initialState = Lifecycle.State.RESUMED,
            fragmentArgs = RequestLoginFragmentArgs(logoutReason = LOGOUT_REASON).toBundle(),
        ) { fragment = this }
        shadowOf(Looper.getMainLooper()).idle()
        val firstView = fragment.requireView()
        val dialog = ShadowDialog.getLatestDialog() as AlertDialog
        assertThat(dialog.isShowing).isTrue()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        shadowOf(Looper.getMainLooper()).idle()
        assertThat(dialog.isShowing).isFalse()

        // Same Fragment, new view: what a back-stack return from troubleshooting does
        fragment.parentFragmentManager.commitNow { detach(fragment) }
        fragment.parentFragmentManager.commitNow { attach(fragment) }
        shadowOf(Looper.getMainLooper()).idle()

        assertThat(fragment.requireView()).isNotSameInstanceAs(firstView)
        assertThat(
            fragment
                .requireView()
                .findViewById<TextView>(R.id.tv_device_id)
                .text
                .toString(),
        ).isEqualTo(context.getString(IDR.string.dashboard_request_login_device_id, FakeCoreModule.DEVICE_ID))
        assertThat(ShadowDialog.getShownDialogs()).hasSize(1)
        assertThat(ShadowDialog.getLatestDialog()).isSameInstanceAs(dialog)
    }

    @Test
    fun `Ten quick taps on the logo navigate to the troubleshooting graph`() {
        every { authStore.signedInProjectId } returns ""
        val navController = testNavController(R.navigation.graph_dashboard, R.id.requestLoginFragment)

        launchFragmentInHiltContainer<RequestLoginFragment>(
            initialState = Lifecycle.State.RESUMED,
            navController = navController,
            fragmentArgs = Bundle(),
        ) {
            tapLogo(times = 10)
        }

        assertThat(navController.currentDestination?.id).isEqualTo(TroubleshootingR.id.troubleshootingFragment)
        assertThat(navController.currentDestination?.parent?.id).isEqualTo(TroubleshootingR.id.graph_troubleshooting)
        // previousBackStackEntry skips graph entries: troubleshooting was pushed on top of request login.
        assertThat(navController.previousBackStackEntry?.destination?.id).isEqualTo(R.id.requestLoginFragment)
    }

    @Test
    fun `Nine quick taps on the logo stay on request login`() {
        every { authStore.signedInProjectId } returns ""
        val navController = testNavController(R.navigation.graph_dashboard, R.id.requestLoginFragment)
        val backStackBefore = navController.backStack.map { it.destination.id }

        launchFragmentInHiltContainer<RequestLoginFragment>(
            initialState = Lifecycle.State.RESUMED,
            navController = navController,
            fragmentArgs = Bundle(),
        ) {
            tapLogo(times = 9)
        }

        assertThat(navController.currentDestination?.id).isEqualTo(R.id.requestLoginFragment)
        assertThat(navController.backStack.map { it.destination.id }).containsExactlyElementsIn(backStackBefore).inOrder()
    }

    @Test
    fun `No logout reason argument shows no dialog`() {
        every { authStore.signedInProjectId } returns ""

        launchFragmentInHiltContainer<RequestLoginFragment>(
            initialState = Lifecycle.State.RESUMED,
            fragmentArgs = RequestLoginFragmentArgs(logoutReason = null).toBundle(),
        )

        assertThat(ShadowDialog.getShownDialogs()).isEmpty()
        assertThat(ShadowDialog.getLatestDialog()).isNull()
    }

    /** Taps back to back on the main thread without advancing the clock, so all taps fall inside the counter window. */
    private fun Fragment.tapLogo(times: Int) {
        val logo = requireView().findViewById<View>(R.id.loginImageViewLogo)
        repeat(times) { logo.performClick() }
    }

    /**
     * Same setup as [launchFragmentInHiltContainer], but keeps the [ActivityScenario] so the test can call `recreate()`,
     * which saves and restores the fragment's instance state like a configuration change.
     */
    private fun launchRecreatableRequestLogin(logoutReason: LogoutReason?): ActivityScenario<HiltTestActivity> {
        val intent = Intent
            .makeMainActivity(ComponentName(context, HiltTestActivity::class.java))
            .putExtra(
                "androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY",
                MR.style.Theme_MaterialComponents,
            )
        val activityScenario = ActivityScenario.launch<HiltTestActivity>(intent)
        scenario = activityScenario
        activityScenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.fragmentFactory.instantiate(
                checkNotNull(RequestLoginFragment::class.java.classLoader),
                RequestLoginFragment::class.java.name,
            )
            fragment.arguments = RequestLoginFragmentArgs(logoutReason = logoutReason).toBundle()
            activity.supportFragmentManager
                .beginTransaction()
                .add(android.R.id.content, fragment, FRAGMENT_TAG)
                .commitNow()
        }
        return activityScenario
    }

    private fun ActivityScenario<HiltTestActivity>.requestLoginFragment(): Fragment {
        lateinit var fragment: Fragment
        onActivity { activity ->
            fragment = checkNotNull(activity.supportFragmentManager.findFragmentByTag(FRAGMENT_TAG))
        }
        assertThat(fragment).isInstanceOf(RequestLoginFragment::class.java)
        return fragment
    }

    private companion object {
        val LOGOUT_REASON = LogoutReason(title = "T", body = "B")
    }
}
