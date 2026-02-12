package com.simprints.infra.events.event.cosync.v1

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.FaceTemplate
import com.simprints.infra.events.event.domain.models.FingerprintTemplate
import kotlinx.serialization.Serializable

/** V1 external schema for face template. */
@Keep
@Serializable
data class FaceTemplateV1(val template: String)

/** V1 external schema for fingerprint template with finger identifier. */
@Keep
@Serializable
data class FingerprintTemplateV1(
    val template: String,
    val finger: SampleIdentifierV1,
)

fun FaceTemplate.toCoSyncV1() = FaceTemplateV1(template = template)
fun FaceTemplateV1.toDomain() = FaceTemplate(template = template)

fun FingerprintTemplate.toCoSyncV1() = FingerprintTemplateV1(
    template = template,
    finger = finger.toCoSyncV1(),
)

fun FingerprintTemplateV1.toDomain() = FingerprintTemplate(
    template = template,
    finger = finger.toDomain(),
)
