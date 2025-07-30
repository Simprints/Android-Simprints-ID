package com.simprints.feature.consent.screens.consent.tempocr

import android.graphics.Rect
import javax.inject.Inject

class OcrBoxMapperUseCase @Inject constructor() {
    operator fun invoke(box: Rect, containerX: Int, containerY: Int): Rect {
        return Rect(
            box.left + containerX,
            box.top + containerY,
            box.right + containerX,
            box.bottom + containerY
        )
    }
}

