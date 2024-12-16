package com.simprints.feature.alert.screen

import androidx.navigation.testing.TestNavHostController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.alert.R
import com.simprints.feature.alert.alertButton
import com.simprints.feature.alert.alertConfiguration
import com.simprints.feature.alert.config.AlertColor
import com.simprints.feature.alert.toArgs
import com.simprints.infra.uibase.navigation.handleResultDirectly
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
            fragmentArgs = alertConfiguration {}.toArgs(),
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
            fragmentArgs = alertConfiguration { color = AlertColor.Red }.toArgs(),
        )

        onView(withChild(withId(R.id.alertTitle))).check(matches(hasBackgroundColor(IDR.color.simprints_red)))
    }

    @Test
    fun `should set raw strings provided with configuration`() {
        launchFragmentInHiltContainer<AlertFragment>(
            navController = navController,
            fragmentArgs = alertConfiguration {
                title = "Custom title"
                message = "Custom message"
                leftButton = alertButton { text = "Custom left" }
                rightButton = alertButton { text = "Custom right" }
            }.toArgs(),
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
            fragmentArgs = alertConfiguration {
                message = "Custom message"
                messageIcon = IDR.drawable.ic_refresh
            }.toArgs(),
        )

        onView(withId(R.id.alertMessage)).check(matches(withText("Custom message")))
        onView(withId(R.id.alertMessage)).check(matches(hasAnyCompoundDrawable()))
    }

    @Test
    fun `should resolve string resources provided with configuration`() {
        // Using random string resources to ensure views get the correct one
        launchFragmentInHiltContainer<AlertFragment>(
            navController = navController,
            fragmentArgs = alertConfiguration {
                titleRes = IDR.string.alert_title_fallback
                messageRes = IDR.string.alert_title_fallback
                leftButton = alertButton { textRes = IDR.string.alert_title_fallback }
                rightButton = alertButton { textRes = IDR.string.alert_title_fallback }
            }.toArgs(),
        )

        onView(withId(R.id.alertTitle)).check(matches(withText(IDR.string.alert_title_fallback)))
        onView(withId(R.id.alertMessage)).check(matches(withText(IDR.string.alert_title_fallback)))
        onView(withId(R.id.alertLeftButton)).check(matches(withText(IDR.string.alert_title_fallback)))
        onView(withId(R.id.alertRightButton)).check(matches(withText(IDR.string.alert_title_fallback)))
    }

    @Test
    fun `notifies caller about back press`() {
        var resultKey: String? = null

        launchFragmentInHiltContainer<AlertFragment>(
            navController = navController,
            fragmentArgs = alertConfiguration {}.toArgs(),
        ) {
            handleResultDirectly<AlertResult>(AlertContract.DESTINATION) { result ->
                resultKey = result.buttonKey
            }
        }
        pressBack()

        Truth.assertThat(resultKey).isEqualTo(AlertContract.ALERT_BUTTON_PRESSED_BACK)
    }

    @Test
    fun `passed back the custom appErrorReason on back press`() {
        var payload: AppErrorReason? = null

        launchFragmentInHiltContainer<AlertFragment>(
            navController = navController,
            fragmentArgs = alertConfiguration {
                appErrorReason = AppErrorReason.LICENSE_INVALID
            }.toArgs(),
        ) {
            handleResultDirectly<AlertResult>(AlertContract.DESTINATION) { result ->
                payload = result.appErrorReason
            }
        }
        pressBack()
        Truth.assertThat(payload).isEqualTo(AppErrorReason.LICENSE_INVALID)
    }

    @Test
    fun `passed back the custom payload on left button press`() {
        var payload: AppErrorReason? = null

        launchFragmentInHiltContainer<AlertFragment>(
            navController = navController,
            fragmentArgs = alertConfiguration {
                appErrorReason = AppErrorReason.LICENSE_INVALID
            }.toArgs(),
        ) {
            handleResultDirectly<AlertResult>(AlertContract.DESTINATION) { result ->
                payload = result.appErrorReason
            }
        }
        onView(withId(R.id.alertLeftButton)).perform(click())
        Truth.assertThat(payload).isEqualTo(AppErrorReason.LICENSE_INVALID)
    }

    @Test
    fun `passed back the custom payload on right button press`() {
        var payload: AppErrorReason? = null

        launchFragmentInHiltContainer<AlertFragment>(
            navController = navController,
            fragmentArgs = alertConfiguration {
                appErrorReason = AppErrorReason.LICENSE_INVALID
                rightButton = alertButton {
                    text = "Right"
                    resultKey = "test"
                }
            }.toArgs(),
        ) {
            handleResultDirectly<AlertResult>(AlertContract.DESTINATION) { result ->
                payload = result.appErrorReason
            }
        }
        onView(withId(R.id.alertRightButton)).perform(click())
        Truth.assertThat(payload).isEqualTo(AppErrorReason.LICENSE_INVALID)
    }

    @Test
    fun `notifies caller about left button press`() {
        var resultKey = ""

        launchFragmentInHiltContainer<AlertFragment>(
            navController = navController,
            fragmentArgs = alertConfiguration {
                leftButton = alertButton {
                    text = "Left"
                    resultKey = "test"
                }
            }.toArgs(),
        ) {
            handleResultDirectly<AlertResult>(AlertContract.DESTINATION) { result ->
                resultKey = result.buttonKey
            }
        }
        onView(withId(R.id.alertLeftButton)).perform(click())
        Truth.assertThat(resultKey).isEqualTo("test")
    }

    @Test
    fun `notifies caller about right button press`() {
        var resultKey = ""

        launchFragmentInHiltContainer<AlertFragment>(
            navController = navController,
            fragmentArgs = alertConfiguration {
                rightButton = alertButton {
                    text = "Right"
                    resultKey = "test"
                }
            }.toArgs(),
        ) {
            handleResultDirectly<AlertResult>(AlertContract.DESTINATION) { result ->
                resultKey = result.buttonKey
            }
        }
        onView(withId(R.id.alertRightButton)).perform(click())
        Truth.assertThat(resultKey).isEqualTo("test")
    }

    @Test
    fun `button does not close screen if not configured`() {
        val navSpy = spyk(navController)
        launchFragmentInHiltContainer<AlertFragment>(
            navController = navSpy,
            fragmentArgs = alertConfiguration {
                leftButton = alertButton {
                    text = "Left"
                    closeOnClick = false
                }
            }.toArgs(),
        ) {
            onView(withId(R.id.alertLeftButton)).perform(click())
        }

        // Since there is no "up" in alert graph, this is the only way to make sure
        // navigation has been called
        verify(exactly = 0) { navSpy.popBackStack() }
    }

    @Test
    fun `button closes screen if configured`() {
        val navSpy = spyk(navController)
        launchFragmentInHiltContainer<AlertFragment>(
            navController = navSpy,
            fragmentArgs = alertConfiguration {
                leftButton = alertButton {
                    text = "Left"
                    closeOnClick = true
                }
            }.toArgs(),
        ) {
            onView(withId(R.id.alertLeftButton)).perform(click())
        }

        // Since there is no "up" in alert graph, this is the only way to make sure
        // navigation has been called
        verify { navSpy.popBackStack() }
    }
}
