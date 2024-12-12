package com.simprints.infra.config.store.local.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.simprints.infra.config.store.local.ConfigLocalDataSourceImpl.Companion.defaultProjectConfiguration
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import java.io.InputStream
import java.io.OutputStream

internal object ProjectConfigurationSerializer : Serializer<ProtoProjectConfiguration> {
    override val defaultValue: ProtoProjectConfiguration = defaultProjectConfiguration

    override suspend fun readFrom(input: InputStream): ProtoProjectConfiguration {
        try {
            return ProtoProjectConfiguration.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: ProtoProjectConfiguration,
        output: OutputStream,
    ) = t.writeTo(output)
}
