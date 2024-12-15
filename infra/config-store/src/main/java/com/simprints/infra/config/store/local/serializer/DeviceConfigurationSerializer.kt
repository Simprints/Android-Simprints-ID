package com.simprints.infra.config.store.local.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.simprints.infra.config.store.local.ConfigLocalDataSourceImpl.Companion.defaultDeviceConfiguration
import com.simprints.infra.config.store.local.models.ProtoDeviceConfiguration
import java.io.InputStream
import java.io.OutputStream

internal object DeviceConfigurationSerializer : Serializer<ProtoDeviceConfiguration> {
    override val defaultValue: ProtoDeviceConfiguration = defaultDeviceConfiguration

    override suspend fun readFrom(input: InputStream): ProtoDeviceConfiguration {
        try {
            return ProtoDeviceConfiguration.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: ProtoDeviceConfiguration,
        output: OutputStream,
    ) = t.writeTo(output)
}
