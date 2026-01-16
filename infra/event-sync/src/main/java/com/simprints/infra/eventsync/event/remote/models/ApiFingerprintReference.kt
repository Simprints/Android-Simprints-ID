package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.eventsync.event.remote.ApiFingerprintTemplate
import com.simprints.infra.eventsync.event.remote.models.ApiBiometricReferenceType.FingerprintReference
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
@ExcludedFromGeneratedTestCoverageReports("API model")
@SerialName("FingerprintReference")
internal data class ApiFingerprintReference(
    override val id: String = UUID.randomUUID().toString(),
    val templates: List<ApiFingerprintTemplate>,
    val format: String,
    val metadata: Map<String, String>? = null,
    override val type: ApiBiometricReferenceType = FingerprintReference,
) : ApiBiometricReference()
