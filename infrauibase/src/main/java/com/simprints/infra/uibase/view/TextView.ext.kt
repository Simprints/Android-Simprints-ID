package com.simprints.infra.uibase.view

import android.widget.TextView
import androidx.annotation.StringRes

fun TextView.setTextWithFallbacks(
    rawText: String?,
    @StringRes textFallback: Int?,
    @StringRes default: Int? = null,
) = when {
    rawText != null -> text = rawText
    textFallback != null -> setText(textFallback)
    default != null -> setText(default)
    else -> text = null
}
