package com.simprints.core.tools.extensions

import android.graphics.Bitmap

fun Bitmap.clone(): Bitmap = copy(config ?: Bitmap.Config.ARGB_8888, isMutable)
