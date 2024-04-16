package com.simprints.feature.clientapi.mappers.request.extractors

import com.simprints.feature.clientapi.extensions.extractString
import java.util.UUID

// TODO PoC
internal open class VerifyIdentityRequestExtractor(val extras: Map<String, Any>) : ActionRequestExtractor(extras) {

    fun getUri(): String = extras.extractString("uri")

    fun getSubjectGuid(): String = extras.extractString("subjectGuid").ifEmpty { UUID.randomUUID().toString() }

    override val expectedKeys = super.keys + listOf("uri")

}
