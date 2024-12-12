package com.simprints.infra.uibase.view

import android.graphics.drawable.Drawable
import com.google.android.material.button.MaterialButton

fun MaterialButton.setCheckedWithLeftDrawable(
    checked: Boolean,
    drawable: Drawable? = null,
) {
    icon = drawable
    isChecked = checked
}
