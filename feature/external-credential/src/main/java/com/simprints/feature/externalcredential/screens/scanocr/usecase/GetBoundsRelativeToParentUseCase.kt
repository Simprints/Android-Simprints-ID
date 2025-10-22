package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Rect
import android.view.View
import javax.inject.Inject

internal class GetBoundsRelativeToParentUseCase @Inject constructor() {
    operator fun invoke(
        parent: View,
        child: View,
    ): Rect {
        val childLocation = IntArray(2)
        val parentLocation = IntArray(2)
        child.getLocationOnScreen(childLocation)
        parent.getLocationOnScreen(parentLocation)

        val offsetX = childLocation[0] - parentLocation[0]
        val offsetY = childLocation[1] - parentLocation[1]

        return Rect(
            offsetX,
            offsetY,
            offsetX + child.width,
            offsetY + child.height,
        )
    }
}
