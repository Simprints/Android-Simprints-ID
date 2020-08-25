package com.simprints.id.tools.serializers

class IntegerSerializer : Serializer<Int> {

    override fun serialize(value: Int): String {
        return value.toString()
    }

    override fun deserialize(string: String): Int {
        return string.toInt()
    }
}
