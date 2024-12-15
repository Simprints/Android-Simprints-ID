package com.simprints.feature.dashboard.tools

import android.view.View
import android.widget.EditText
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.material.chip.Chip
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf

fun typeSearchViewText(text: String): ViewAction = object : ViewAction {
    override fun getDescription(): String = "Change view text"

    override fun getConstraints(): Matcher<View> = AllOf.allOf(
        ViewMatchers.isDisplayed(),
        ViewMatchers.isAssignableFrom(EditText::class.java),
    )

    override fun perform(
        uiController: UiController?,
        view: View?,
    ) {
        (view as EditText).setText(text)
    }
}

fun clickCloseChipIcon(): ViewAction = object : ViewAction {
    override fun getConstraints(): Matcher<View> = ViewMatchers.isAssignableFrom(Chip::class.java)

    override fun getDescription(): String = "click drawable "

    override fun perform(
        uiController: UiController,
        view: View,
    ) {
        val chip = view as Chip
        chip.performCloseIconClick()
    }
}
