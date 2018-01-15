package com.simprints.id.tools.serializers

import com.simprints.id.domain.Location


class LocationSerializer: Serializer<Location> {

    override fun serialize(value: Location): String =
        "${value.latitude}_${value.longitude}"

    override fun deserialize(string: String): Location {
        val (latitude, longitude) = string.split("_")
        return Location(latitude, longitude)
    }
}
