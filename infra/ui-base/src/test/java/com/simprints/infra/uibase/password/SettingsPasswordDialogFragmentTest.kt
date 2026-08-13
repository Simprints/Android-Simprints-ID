package com.simprints.infra.uibase.password

import androidx.fragment.app.testing.launchFragment
import androidx.test.espresso.*
import androidx.test.espresso.action.*
import androidx.test.espresso.assertion.*
import androidx.test.espresso.matcher.*
import androidx.test.ext.junit.runners.*
import com.simprints.infra.uibase.R
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
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
                // onSuccess = { fail() }
            )
        }
        Espresso
            .onView(ViewMatchers.withId(android.R.id.button2))
            .inRoot(RootMatchers.isDialog())
            .perform(ViewActions.click())
    }

    @Test
    fun `shows error if incorrect password`() {
        launchFragment(themeResId = MR.style.Theme_MaterialComponents) {
            SettingsPasswordDialogFragment.newInstance(
                passwordToMatch = "1234",
                // onSuccess = { fail() }
            )
        }

        Espresso
            .onView(ViewMatchers.withId(R.id.password_input_field))
            .inRoot(RootMatchers.isDialog())
            .perform(ViewActions.replaceText("1111"))

        Espresso
            .onView(ViewMatchers.withId(MR.id.textinput_error))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.withText(IDR.string.dashboard_password_lock_wrong_pin)))

        Espresso
            .onView(ViewMatchers.withId(R.id.password_input_field))
            .check(ViewAssertions.matches(ViewMatchers.withText("")))
    }

    @Test
    fun `resets error on new password attempt`() {
        launchFragment(themeResId = MR.style.Theme_MaterialComponents) {
            SettingsPasswordDialogFragment.newInstance(
                passwordToMatch = "1234",
                //  onSuccess = { fail() }
            )
        }

        Espresso
            .onView(ViewMatchers.withId(R.id.password_input_field))
            .inRoot(RootMatchers.isDialog())
            .perform(ViewActions.replaceText("1111"))

        Espresso
            .onView(ViewMatchers.withId(MR.id.textinput_error))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.withText(IDR.string.dashboard_password_lock_wrong_pin)))

        Espresso
            .onView(ViewMatchers.withId(R.id.password_input_field))
            .inRoot(RootMatchers.isDialog())
            .perform(ViewActions.replaceText("12"))

        Espresso
            .onView(ViewMatchers.withId(MR.id.textinput_error))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(ViewMatchers.withText("")))
    }

    @Test
    fun `triggers callback when password matches`() = runTest {
        suspendCancellableCoroutine { cont ->
            launchFragment(themeResId = MR.style.Theme_MaterialComponents) {
                SettingsPasswordDialogFragment.newInstance(
                    passwordToMatch = "1234",
                    // onSuccess = { cont.resume(Unit) }
                )
            }

            Espresso
                .onView(ViewMatchers.withId(R.id.password_input_field))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.replaceText("1234"))
        }
    }
}
