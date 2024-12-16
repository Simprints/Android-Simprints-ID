package com.simprints.core.tools.extentions

import android.content.Context
import android.util.TypedValue
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports

@ExcludedFromGeneratedTestCoverageReports("UI code")
fun Float.dpToPx(context: Context) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics)
