package com.simprints.id.tools.serializers

import java.security.InvalidParameterException

/**
 * Simple serializer for Boolean objects.
 *
 * @author: Etienne Thiery (etienne@simprints.com)
 */
class BooleanSerializer : Serializer<Boolean> {

    companion object {
        private val booleanToString = mapOf(true to "true", false to "false")
        private val stringToBoolean = booleanToString.entries
                .map { Pair(it.value, it.key) }
                .toMap()
    }

    override fun serialize(value: Boolean): String =
            booleanToString[value]!!


    override fun deserialize(string: String): Boolean =
            stringToBoolean[string] ?: throw InvalidParameterException("Invalid serialized boolean")
}