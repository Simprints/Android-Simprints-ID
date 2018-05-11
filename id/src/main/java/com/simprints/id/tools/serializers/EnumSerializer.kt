package com.simprints.id.tools.serializers

import java.security.InvalidParameterException


class EnumSerializer<T : Enum<*>>(private val enumClass: Class<T>): Serializer<T> {

    private val stringToEnumValue = enumClass.enumConstants
            .map { Pair(it.name, it) }
            .toMap()

    override fun serialize(value: T): String =
            value.name

    override fun deserialize(string: String): T =
            stringToEnumValue[string] ?: throw InvalidParameterException("Invalid serialized enum value")

    fun deserialize(index: Int): T {
        return enumClass.enumConstants[index]
    }
}
