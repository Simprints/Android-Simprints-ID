package com.simprints.id.tools.serializers

import java.security.InvalidParameterException

/**
 * @author: Etienne Thiery (etienne@simprints.com)
 */
class EnumSerializer<T : Enum<*>>(enumClass: Class<T>): Serializer<T> {

    private val stringToEnumValue = enumClass.enumConstants
            .map { Pair(it.name, it) }
            .toMap()

    override fun serialize(value: T): String =
            value.name

    override fun deserialize(string: String): T =
            stringToEnumValue[string] ?: throw InvalidParameterException("Invalid serialized enum value")
}