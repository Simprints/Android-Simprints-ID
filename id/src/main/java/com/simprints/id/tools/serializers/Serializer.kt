package com.simprints.id.tools.serializers

/**
 * @author: Etienne Thiery (etienne@simprints.com)
 */
interface Serializer<T: Any>{

    fun serialize(value: T): String
    fun deserialize(string: String): T

}

