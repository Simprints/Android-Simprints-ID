package com.simprints.infra.uibase.extensions

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.showToast(
    @StringRes resId: Int,
    duration: Int = Toast.LENGTH_LONG,
) {
    Toast.makeText(this, resId, duration).show()
}

fun Context.showToast(
    string: String,
    duration: Int = Toast.LENGTH_LONG,
) = Toast.makeText(this, string, duration).show()
