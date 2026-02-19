package com.simprints.infra.events.event.cosync.v1

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.FaceTemplate
import com.simprints.infra.events.event.domain.models.FingerprintTemplate
import kotlinx.serialization.Serializable

/** V1 external schema for face template. */
@Keep
@Serializable
data class CoSyncFaceTemplate(val template: String)

/** V1 external schema for fingerprint template with finger identifier. */
@Keep
@Serializable
data class CoSyncFingerprintTemplate(
    val template: String,
    val finger: CoSyncTemplateIdentifier,
)

fun FaceTemplate.toCoSync() = CoSyncFaceTemplate(template = template)
fun CoSyncFaceTemplate.toEventDomain() = FaceTemplate(template = template)

fun FingerprintTemplate.toCoSync() = CoSyncFingerprintTemplate(
    template = template,
    finger = finger.toCoSync(),
)

fun CoSyncFingerprintTemplate.toEventDomain() = FingerprintTemplate(
    template = template,
    finger = finger.toDomain(),
)
