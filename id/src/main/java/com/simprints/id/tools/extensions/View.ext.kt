package com.simprints.id.tools.extensions

import android.view.View

var android.widget.TextView.textColor: Int
    get() = throw IllegalAccessException("no getter for text color")
    set(v) = setTextColor(v)

fun View.onLayoutChange(l: View.OnLayoutChangeListener) = addOnLayoutChangeListener(l)
