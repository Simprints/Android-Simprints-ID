package com.simprints.ear.infra.earsdk

import com.simprints.ear.infra.basebiosdk.matching.EarMatcher
import com.simprints.simface.core.SimFaceFacade
import javax.inject.Inject

class EarSimFaceMatcher @Inject constructor(
    private val simFace: SimFaceFacade
) : EarMatcher() {

    override val matcherName: String
        get() = "EAR_SIM_FACE"

    override val supportedTemplateFormat: String
        get() = EarSimFaceDetector.EAR_SIM_FACE_TEMPLATE

    override suspend fun getComparisonScore(probe: ByteArray, matchAgainst: ByteArray): Float {
        // Verify the embedding against itself
        return simFace.matchProcessor.verificationScore(probe, matchAgainst)
    }
}
