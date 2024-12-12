package com.simprints.feature.orchestrator.cache

import android.os.Bundle
import androidx.core.os.bundleOf
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.io.IOException

class BundleSerializer : JsonSerializer<Bundle>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(
        value: Bundle,
        gen: JsonGenerator,
        serializers: SerializerProvider,
    ) {
        val map = value.keySet().associateWith { key -> value.get(key) }
        gen.writeObject(map)
    }
}

class BundleDeserializer : JsonDeserializer<Bundle>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): Bundle {
        val mapType = ctxt.typeFactory.constructMapType(Map::class.java, String::class.java, Any::class.java)
        val map: Map<String, Any> = ctxt.readValue(p, mapType)
        // Serializer adds a 'type' key-value pair for unmarshalling purposes. Removing this artifact.
        val filteredMap = map.filterKeys { key -> key != "type" }
        return bundleOf(*filteredMap.toList().toTypedArray())
    }
}
