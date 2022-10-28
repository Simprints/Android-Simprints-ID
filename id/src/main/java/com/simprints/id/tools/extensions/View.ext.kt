package com.simprints.id.tools.extensions

var android.widget.TextView.textColor: Int
    get() = throw IllegalAccessException("no getter for text color")
    set(v) = setTextColor(v)
