package com.simprints.testtools.android

import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers


open class BaseAssertions {

    fun assertToastMessageIs(@StringRes messageId: Int) {
        onView(withText(messageId))
            .inRoot(RootMatchers.withDecorView(Matchers.not((getCurrentActivity()?.window?.decorView))))
            .check(matches(isDisplayed()))
    }
}
