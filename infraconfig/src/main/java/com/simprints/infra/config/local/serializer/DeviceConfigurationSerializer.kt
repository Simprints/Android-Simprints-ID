package com.simprints.infra.config.local.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.simprints.infra.config.local.models.ProtoDeviceConfiguration
import java.io.InputStream
import java.io.OutputStream

@Suppress("BlockingMethodInNonBlockingContext")
internal object DeviceConfigurationSerializer : Serializer<ProtoDeviceConfiguration> {
    override val defaultValue: ProtoDeviceConfiguration =
        ProtoDeviceConfiguration.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ProtoDeviceConfiguration {
        try {
            return ProtoDeviceConfiguration.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: ProtoDeviceConfiguration, output: OutputStream) =
        t.writeTo(output)
}
