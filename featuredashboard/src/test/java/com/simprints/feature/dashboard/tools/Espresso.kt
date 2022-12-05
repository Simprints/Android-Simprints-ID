package com.simprints.feature.dashboard.tools

import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.material.chip.Chip
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf

fun typeSearchViewText(text: String): ViewAction {
    return object : ViewAction {
        override fun getDescription(): String {
            return "Change view text"
        }

        override fun getConstraints(): Matcher<View> {
            return AllOf.allOf(
                ViewMatchers.isDisplayed(),
                ViewMatchers.isAssignableFrom(SearchView::class.java)
            )
        }

        override fun perform(uiController: UiController?, view: View?) {
            (view as SearchView).setQuery(text, false)
        }
    }
}

fun clickCloseChipIcon(): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isAssignableFrom(Chip::class.java)
        }

        override fun getDescription(): String {
            return "click drawable "
        }

        override fun perform(uiController: UiController, view: View) {
            val chip = view as Chip
            chip.performCloseIconClick()
        }
    }
}
