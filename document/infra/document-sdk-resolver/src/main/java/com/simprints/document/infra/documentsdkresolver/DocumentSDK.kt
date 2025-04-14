package com.simprints.document.infra.documentsdkresolver

import com.simprints.document.infra.basedocumentsdk.detection.DocumentDetector
import com.simprints.document.infra.basedocumentsdk.initialization.DocumentSdkInitializer
import com.simprints.document.infra.basedocumentsdk.matching.DocumentMatcher
import com.simprints.document.infra.basedocumentsdk.matching.DocumentSample

interface DocumentSDK {
    val initializer: DocumentSdkInitializer
    val detector: DocumentDetector

    val version: String
    val templateFormat: String
    val matcherName: String

    fun createMatcher(probeSamples: List<DocumentSample>): DocumentMatcher
}
