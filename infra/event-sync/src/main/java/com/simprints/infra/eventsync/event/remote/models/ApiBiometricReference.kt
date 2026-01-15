package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.TemplateIdentifier
import com.simprints.infra.events.event.domain.models.FaceTemplate
import com.simprints.infra.events.event.domain.models.FingerprintTemplate
import com.simprints.infra.eventsync.event.remote.ApiFingerprintTemplate
import kotlinx.serialization.Serializable
import com.simprints.infra.events.event.domain.models.FaceReference as DomainFaceReference
import com.simprints.infra.events.event.domain.models.FingerprintReference as DomainFingerprintReference

@ExcludedFromGeneratedTestCoverageReports("API model")
@Serializable
internal sealed class ApiBiometricReference {
    abstract val type: ApiBiometricReferenceType
    abstract val id: String
}

@Keep
@Serializable
internal enum class ApiBiometricReferenceType {
    FaceReference,
    FingerprintReference,
}

internal fun ApiBiometricReference.fromApiToDomain() = when (this.type) {
    ApiBiometricReferenceType.FaceReference -> (this as ApiFaceReference).fromApiToDomain()
    ApiBiometricReferenceType.FingerprintReference -> (this as ApiFingerprintReference).fromApiToDomain()
}

internal fun ApiFaceReference.fromApiToDomain() = DomainFaceReference(id, templates.map { it.fromApiToDomain() }, format, metadata)

internal fun ApiFaceTemplate.fromApiToDomain() = FaceTemplate(template)

internal fun FaceTemplate.fromDomainToApi() = ApiFaceTemplate(template)

internal fun ApiFingerprintReference.fromApiToDomain() =
    DomainFingerprintReference(id, templates.map { it.fromApiToDomain() }, format, metadata)

internal fun ApiFingerprintTemplate.fromApiToDomain() = FingerprintTemplate(template, TemplateIdentifier.valueOf(finger.name))
