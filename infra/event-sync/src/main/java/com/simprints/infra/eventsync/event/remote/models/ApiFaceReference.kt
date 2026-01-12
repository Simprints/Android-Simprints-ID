package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.eventsync.event.remote.models.ApiBiometricReferenceType.FaceReference
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@ExcludedFromGeneratedTestCoverageReports("API model")
@Keep
@Serializable
@SerialName("FaceReference")
internal data class ApiFaceReference(
    override val id: String = UUID.randomUUID().toString(),
    val templates: List<ApiFaceTemplate>,
    val format: String,
    val metadata: Map<String, String>? = null,
    override val type: ApiBiometricReferenceType = FaceReference,
) : ApiBiometricReference()
