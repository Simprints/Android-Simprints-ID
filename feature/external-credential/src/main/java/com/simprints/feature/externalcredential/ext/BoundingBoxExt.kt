  package com.simprints.feature.externalcredential.ext

import android.graphics.Rect
import com.simprints.feature.externalcredential.model.BoundingBox

internal fun Rect.toBoundingBox(): BoundingBox = BoundingBox(left, top, right, bottom)

internal fun BoundingBox.toRect(): Rect = Rect(left, top, right, bottom)
