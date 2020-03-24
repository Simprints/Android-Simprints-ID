package com.simprints.core.tools.extentions

import android.graphics.drawable.Drawable
import android.widget.CheckedTextView

fun CheckedTextView.setCheckedWithLeftDrawable(checked: Boolean, drawable: Drawable? = null) {
    setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    isChecked = checked
}
