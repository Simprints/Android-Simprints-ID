package com.simprints.feature.alert

import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.testing.TestNavHostController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.feature.alert.config.AlertColor
import com.simprints.testtools.common.syntax.hasAnyCompoundDrawable
import com.simprints.testtools.common.syntax.hasBackgroundColor
import com.simprints.testtools.hilt.launchFragmentInHiltContainer
import com.simprints.testtools.hilt.testNavController
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.spyk
import io.mockk.verify
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import com.simprints.infra.resources.R as IDR

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class AlertFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        navController = testNavController(R.navigation.graph_alert)
    }

    @Test
    fun `should set correct visibility for default values`() {
        launchFragmentInHiltContainer<AlertFragment>(
            navController = navController,
            fragmentArgs = alertConfigurationArgs {}
        )

        onView(withChild(withId(R.id.alertTitle))).check(matches(hasBackgroundColor(IDR.color.simprints_blue)))
        onView(withId(R.id.alertTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.alertImage)).check(matches(isDisplayed()))
        onView(withId(R.id.alertLeftButton)).check(matches(isDisplayed()))

        // No default value for message
        onView(withId(R.id.alertMessage)).check(matches(isDisplayed())).check(matches(withText("")))
        onView(withId(R.id.alertMessage)).check(matches(not(hasAnyCompoundDrawable())))

        // Right button shown only if provided explicitly
        onView(withId(R.id.alertRightButton)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should set correct background`() {
        launchFragmentInHiltContainer<AlertFragment>(
            navController = navController,
            fragmentArgs = alertConfigurationArgs { color = AlertColor.Red }
        )

        onView(withChild(withId(R.id.alertTitle))).check(matches(hasBackgroundColor(IDR.color.simprints_red)))
    }

    @Test
    fun `should set raw strings provided with configuration`() {
        launchFragmentInHiltContainer<AlertFragment>(
            navController = navController,
            fragmentArgs = alertConfigurationArgs {
                title = "Custom title"
                message = "Custom message"
                leftButton = alertButton { text = "Custom left" }
                rightButton = alertButton { text = "Custom right" }
            }
        )

        onView(withId(R.id.alertTitle)).check(matches(withText("Custom title")))
        onView(withId(R.id.alertMessage)).check(matches(withText("Custom message")))
        onView(withId(R.id.alertLeftButton)).check(matches(withText("Custom left")))
        onView(withId(R.id.alertRightButton)).check(matches(withText("Custom right")))
    }

    @Test
    fun `message has icon if provided with configuration`() {
        launchFragmentInHiltContainer<AlertFragment>(
            navController = navController,
            fragmentArgs = alertConfigurationArgs {
                message = "Custom message"
                messageIcon = IDR.drawable.ic_refresh
            }
        )

        onView(withId(R.id.alertMessage)).check(matches(withText("Custom message")))
        onView(withId(R.id.alertMessage)).check(matches(hasAnyCompoundDrawable()))
    }

    @Test
    fun `should resolve string resources provided with configuration`() {
        // Using random string resources to ensure views get the correct one
        launchFragmentInHiltContainer<AlertFragment>(
            navController = navController,
            fragmentArgs = alertConfigurationArgs {
                titleRes = IDR.string.app_name
                messageRes = IDR.string.unforeseen_error_message
                leftButton = alertButton { textRes = IDR.string.login_server_error }
                rightButton = alertButton { textRes = IDR.string.error_occurred_title }
            }
        )

        onView(withId(R.id.alertTitle)).check(matches(withText(IDR.string.app_name)))
        onView(withId(R.id.alertMessage)).check(matches(withText(IDR.string.unforeseen_error_message)))
        onView(withId(R.id.alertLeftButton)).check(matches(withText(IDR.string.login_server_error)))
        onView(withId(R.id.alertRightButton)).check(matches(withText(IDR.string.error_occurred_title)))
    }

    @Test
    fun `notifies caller about back press`() {
        var resultKey: String? = null

        launchFragmentInHiltContainer<AlertFragment>(
            navController = navController,
            fragmentArgs = alertConfigurationArgs {}
        ) {
            setFragmentResultListener(AlertFragment.ALERT_REQUEST) { _, data ->
                resultKey = data.getString(AlertFragment.ALERT_BUTTON_PRESSED)
            }
        }
        pressBack()

        Truth.assertThat(resultKey).isEqualTo(AlertFragment.ALERT_BUTTON_PRESSED_BACK)
    }

    @Test
    fun `notifies caller about left button press`() {
        var resultKey: String? = null

        launchFragmentInHiltContainer<AlertFragment>(
            navController = navController,
            fragmentArgs = alertConfigurationArgs {
                leftButton = alertButton {
                    text = "Left"
                    resultKey = "test"
                }
            }
        ) {
            setFragmentResultListener(AlertFragment.ALERT_REQUEST) { _, data ->
                resultKey = data.getString(AlertFragment.ALERT_BUTTON_PRESSED)
            }
        }
        onView(withId(R.id.alertLeftButton)).perform(click())
        Truth.assertThat(resultKey).isEqualTo("test")
    }

    @Test
    fun `notifies caller about right button press`() {
        var resultKey: String? = null

        launchFragmentInHiltContainer<AlertFragment>(
            navController = navController,
            fragmentArgs = alertConfigurationArgs {
                rightButton = alertButton {
                    text = "Right"
                    resultKey = "test"
                }
            }
        ) {
            setFragmentResultListener(AlertFragment.ALERT_REQUEST) { _, data ->
                resultKey = data.getString(AlertFragment.ALERT_BUTTON_PRESSED)
            }
        }
        onView(withId(R.id.alertRightButton)).perform(click())
        Truth.assertThat(resultKey).isEqualTo("test")
    }

    @Test
    fun `button closes screen if configured`() {
        val navSpy = spyk(navController)
        launchFragmentInHiltContainer<AlertFragment>(
            navController = navSpy,
            fragmentArgs = alertConfigurationArgs {
                leftButton = alertButton {
                    text = "Left"
                    closeOnClick = true
                }
            }
        ) {
            onView(withId(R.id.alertLeftButton)).perform(click())
        }

        // Since there is no "up" in alert graph, this is the only way to make sure
        // navigation has been called
        verify { navSpy.navigateUp() }
    }
}
