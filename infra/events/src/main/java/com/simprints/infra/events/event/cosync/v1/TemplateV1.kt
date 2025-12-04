package com.simprints.infra.events.event.cosync.v1

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.simprints.infra.events.event.domain.models.subject.FaceTemplate
import com.simprints.infra.events.event.domain.models.subject.FingerprintTemplate

/**
 * V1 external schema for face template.
 *
 * Contains base64-encoded biometric template data.
 */
@Keep
@JsonInclude(JsonInclude.Include.NON_NULL)
data class FaceTemplateV1(
    /**
     * Base64-encoded face template.
     */
    val template: String,
)

/**
 * V1 external schema for fingerprint template.
 *
 * Contains base64-encoded biometric template data with finger identifier.
 */
@Keep
@JsonInclude(JsonInclude.Include.NON_NULL)
data class FingerprintTemplateV1(
    /**
     * Base64-encoded fingerprint template.
     */
    val template: String,

    /**
     * Identifier for which finger this template belongs to.
     */
    val finger: SampleIdentifierV1,
)

/**
 * Converts internal FaceTemplate to V1 external schema.
 */
fun FaceTemplate.toCoSyncV1() = FaceTemplateV1(template = template)

/**
 * Converts V1 external schema to internal FaceTemplate.
 */
fun FaceTemplateV1.toDomain() = FaceTemplate(template = template)

/**
 * Converts internal FingerprintTemplate to V1 external schema.
 */
fun FingerprintTemplate.toCoSyncV1() = FingerprintTemplateV1(
    template = template,
    finger = finger.toCoSyncV1(),
)

/**
 * Converts V1 external schema to internal FingerprintTemplate.
 */
fun FingerprintTemplateV1.toDomain() = FingerprintTemplate(
    template = template,
    finger = finger.toDomain(),
)
