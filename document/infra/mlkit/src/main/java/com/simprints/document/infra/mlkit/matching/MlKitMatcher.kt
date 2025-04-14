package com.simprints.document.infra.mlkit.matching

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.document.infra.basedocumentsdk.matching.DocumentIdentity
import com.simprints.document.infra.basedocumentsdk.matching.DocumentMatcher
import com.simprints.document.infra.basedocumentsdk.matching.DocumentSample

@ExcludedFromGeneratedTestCoverageReports(
    reason = "TODO evaluate for MLkit",
)
class MlKitMatcher(
    override val probeSamples: List<DocumentSample>
) : DocumentMatcher(probeSamples) {

    var probeTemplates: List<DocumentIdentity> = probeSamples.mapIndexed { i, probe ->
        // todo
    }

    override suspend fun getHighestComparisonScoreForCandidate(candidate: DocumentIdentity): Float =
        probeTemplates.flatMap { probeTemplate ->
            candidate.documents.map { face ->
                getSimilarityScoreForCandidate(probeTemplate, face.template)
            }
        }.max()

    private fun getSimilarityScoreForCandidate(
        probeTemplate: DocumentIdentity,
        candidateTemplate: ByteArray
    ): Float {
        // todo
    }

    override fun close() {
        // todo
    }
}
