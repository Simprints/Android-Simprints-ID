package com.simprints.core.tools.extentions

import android.graphics.RectF
import kotlin.math.abs

fun RectF.area() = abs(height() * width())
