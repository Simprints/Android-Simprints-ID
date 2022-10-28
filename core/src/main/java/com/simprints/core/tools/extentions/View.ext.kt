package com.simprints.core.tools.extentions

import android.view.View

// This ext fun is created to avoid changing in all files that was using a anko which is a deprecated lib
fun View.onLayoutChange(l: View.OnLayoutChangeListener) = addOnLayoutChangeListener(l)
