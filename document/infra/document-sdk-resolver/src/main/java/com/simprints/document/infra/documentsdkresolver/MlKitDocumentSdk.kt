package com.simprints.document.infra.documentsdkresolver

import com.simprints.document.infra.basedocumentsdk.matching.DocumentMatcher
import com.simprints.document.infra.basedocumentsdk.matching.DocumentSample
import com.simprints.document.infra.mlkit.detection.MlKitDetector
import com.simprints.document.infra.mlkit.detection.MlKitDetector.Companion.RANK_ONE_TEMPLATE_FORMAT_1_23
import com.simprints.document.infra.mlkit.initialization.MlKitInitializer
import com.simprints.document.infra.mlkit.matching.MlKitMatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MlKitDocumentSdk @Inject constructor(
    override val initializer: MlKitInitializer,
    override val detector: MlKitDetector,
) : DocumentSDK {
    override val version: String = "16"
    override val templateFormat: String = "" // todo
    override val matcherName: String = "MLkit"

    override fun createMatcher(probeSamples: List<DocumentSample>): DocumentMatcher = MlKitMatcher(probeSamples)
}
