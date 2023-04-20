package com.simprints.id.tools.extensions

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

fun Activity.runOnUiThreadIfStillRunning(then: () -> Unit) {
    runOnUiThreadIfStillRunning(then) {}
}

fun Activity.runOnUiThreadIfStillRunning(then: () -> Unit, otherwise: () -> Unit) {
    if (!isFinishing) {
        this.runOnUiThread { then() }
    } else {
        otherwise()
    }
}

fun Activity.showToast(stringRes: Int) =
    runOnUiThread {
        Toast.makeText(this, getString(stringRes), Toast.LENGTH_LONG).show()
    }

fun Activity.showKeyboard(target: View) {
    val ime = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
    ime.showSoftInput(target, InputMethodManager.SHOW_IMPLICIT)
}
