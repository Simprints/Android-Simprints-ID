package com.simprints.feature.dashboard.tools

import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.simprints.infra.uibase.R as UiBaseR

/** Types into the settings password dialog; the dialog checks the password on every change. */
internal fun enterSettingsPassword(password: String) {
    onView(withId(UiBaseR.id.password_input_field))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
        .perform(replaceText(password))
}

internal fun PreferenceFragmentCompat.preference(keyRes: Int): Preference = requireNotNull(findPreference(getString(keyRes)))
