package com.simprints.infra.eventsync.event.remote.models.session

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.scope.Location
import com.simprints.infra.eventsync.event.remote.models.ApiTimestamp
import com.simprints.infra.eventsync.event.remote.models.fromDomainToApi
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiLocation(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    val lastLocationTime: ApiTimestamp? = null,
    var noPermission: Boolean? = null,
)

internal fun Location?.fromDomainToApi() = this?.let {
    ApiLocation(
        latitude = latitude,
        longitude = longitude,
        lastLocationTime = lastLocationTime?.fromDomainToApi(),
        noPermission = noPermission,
    )
}
