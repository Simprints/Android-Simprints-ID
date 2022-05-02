package com.simprints.id.tools.extensions

import android.view.View

var android.widget.TextView.textColor: Int
    get() = throw IllegalAccessException("no getter for text color")
    set(v) = setTextColor(v)

// This ext fun is created to avoid changing in all files that was using a anko which is a deprecated lib
fun View.onLayoutChange(l: View.OnLayoutChangeListener) = addOnLayoutChangeListener(l)
