package com.simprints.feature.dashboard.settings.password

import androidx.fragment.app.testing.launchFragment
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.RootMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.feature.dashboard.R
import kotlinx.coroutines.test.runTest
import org.junit.Assert.fail
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import com.google.android.material.R as MR
import com.simprints.infra.resources.R as IDR

@Ignore("launchFragment does not support fragments built with factory methods")
@RunWith(AndroidJUnit4::class)
class SettingsPasswordDialogFragmentTest {

    @Test
    fun `closes without success on cancel`() {
        launchFragment(themeResId = MR.style.Theme_MaterialComponents) {
            SettingsPasswordDialogFragment.newInstance(
                passwordToMatch = "1234",
                //onSuccess = { fail() }
            )
        }
        onView(withId(android.R.id.button2))
            .inRoot(isDialog())
            .perform(click())
    }

    @Test
    fun `shows error if incorrect password`() {
        launchFragment(themeResId = MR.style.Theme_MaterialComponents) {
            SettingsPasswordDialogFragment.newInstance(
                passwordToMatch = "1234",
                // onSuccess = { fail() }
            )
        }

        onView(withId(R.id.password_input_field))
            .inRoot(isDialog())
            .perform(replaceText("1111"))

        onView(withId(MR.id.textinput_error))
            .check(matches(isDisplayed()))
            .check(matches(withText(IDR.string.dashboard_password_lock_wrong_pin)))

        onView(withId(R.id.password_input_field))
            .check(matches(withText("")))
    }

    @Test
    fun `resets error on new password attempt`() {
        launchFragment(themeResId = MR.style.Theme_MaterialComponents) {
            SettingsPasswordDialogFragment.newInstance(
                passwordToMatch = "1234",
                //  onSuccess = { fail() }
            )
        }

        onView(withId(R.id.password_input_field))
            .inRoot(isDialog())
            .perform(replaceText("1111"))

        onView(withId(MR.id.textinput_error))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .check(matches(withText(IDR.string.dashboard_password_lock_wrong_pin)))

        onView(withId(R.id.password_input_field))
            .inRoot(isDialog())
            .perform(replaceText("12"))

        onView(withId(MR.id.textinput_error))
            .inRoot(isDialog())
            .check(matches(withText("")))
    }

    @Test
    fun `triggers callback when password matches`() = runTest {
        suspendCoroutine { cont ->
            launchFragment(themeResId = MR.style.Theme_MaterialComponents) {
                SettingsPasswordDialogFragment.newInstance(
                    passwordToMatch = "1234",
                    // onSuccess = { cont.resume(Unit) }
                )
            }

            onView(withId(R.id.password_input_field))
                .inRoot(isDialog())
                .perform(replaceText("1234"))
        }
    }
}
