package com.simprints.id.tools.serializers

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Simple Map serializer.
 *
 * If you manage to write a generic Map serializer that implements the Serializer interface,
 * shoot me an email because I am very much interested!
 */
class MapSerializer<K: Any, V: Any>(private val kSerializer: Serializer<K>,
                                    private val vSerializer: Serializer<V>,
                                    private val gson: Gson)
    : Serializer<Map<K, V>> {

    companion object {
        val stringMapType: Type = object : TypeToken<Map<String, String>>() {}.type
    }

    override fun serialize(value: Map<K, V>): String {
        val stringMap = value.entries
                .map { Pair(kSerializer.serialize(it.key), vSerializer.serialize(it.value)) }
                .toMap()
        return gson.toJson(stringMap)
    }

    override fun deserialize(string: String): Map<K, V> {
        val stringMap: Map<String, String> = gson.fromJson(string, stringMapType)
        return stringMap.entries
                .map { Pair(kSerializer.deserialize(it.key), vSerializer.deserialize(it.value)) }
                .toMap()
    }
}
