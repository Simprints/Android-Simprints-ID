package com.simprints.id.tools.serializers


interface Serializer<T: Any>{

    fun serialize(value: T): String
    fun deserialize(string: String): T

}

