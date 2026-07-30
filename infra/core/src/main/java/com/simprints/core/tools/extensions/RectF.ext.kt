package com.simprints.core.tools.extensions

import android.graphics.RectF
import kotlin.math.abs

fun RectF.area() = abs(height() * width())
