package com.simprints.infra.events.event.domain.models.scope

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Location(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    val lastLocationTime: Long? = null,
    val noPermission: Boolean? = null,
) {
    companion object {
        val NO_PERMISSION = Location(noPermission = true)
    }
}
