package com.simprints.feature.clientapi.mappers.request.extractors

import com.simprints.feature.clientapi.extensions.extractString
import java.util.UUID

// TODO PoC
internal open class VerifyIdentityRequestExtractor(val extras: Map<String, Any>) : ActionRequestExtractor(extras) {

    fun getImage(): String = extras.extractString("image")

    fun getSubjectGuid(): String = extras.extractString("subjectGuid").ifEmpty { UUID.randomUUID().toString() }

    override val expectedKeys = super.keys + listOf("image")

}
