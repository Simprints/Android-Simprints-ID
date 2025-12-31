package com.simprints.infra.eventsync.event.remote.models.session

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.scope.Location
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiLocation(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
)

internal fun Location?.fromDomainToApi() = this?.let { ApiLocation(latitude, longitude) }
