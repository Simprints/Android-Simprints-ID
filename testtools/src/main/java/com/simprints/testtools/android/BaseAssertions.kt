package com.simprints.testtools.android

import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText

open class BaseAssertions {

    fun assertToastMessageIs(@StringRes messageId: Int) {
        onToast(messageId).check(matches(isDisplayed()))
    }

    private fun onToast(@StringRes messageId: Int): ViewInteraction {
        return onView(withText(messageId)).inToast()
    }

}
