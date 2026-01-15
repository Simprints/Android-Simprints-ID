package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.BiometricDataSource
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal enum class ApiBiometricDataSource {
    SIMPRINTS,
    COMMCARE,
}

internal fun BiometricDataSource.fromDomainToApi(): ApiBiometricDataSource = when (this) {
    BiometricDataSource.SIMPRINTS -> ApiBiometricDataSource.SIMPRINTS
    BiometricDataSource.COMMCARE -> ApiBiometricDataSource.COMMCARE
}
