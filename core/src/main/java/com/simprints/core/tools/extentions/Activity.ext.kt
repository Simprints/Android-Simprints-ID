package com.simprints.core.tools.extentions

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

fun Activity.hideKeyboard() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    var focusedView = currentFocus
    if (focusedView == null)
        focusedView = View(this)
    val flags = 0
    inputMethodManager.hideSoftInputFromWindow(focusedView.windowToken, flags)
}

fun Activity.removeAnimationsToNextActivity() =
    overridePendingTransition(0, 0)
