package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import com.simprints.feature.externalcredential.screens.scanocr.model.LightingConditionsAssessment
import javax.inject.Inject

internal class GetLightingConditionsAssessmentUseCase @Inject constructor() {
    operator fun invoke(bitmap: Bitmap): LightingConditionsAssessment {
        // todo implement
        return LightingConditionsAssessment.NORMAL
    }
}
