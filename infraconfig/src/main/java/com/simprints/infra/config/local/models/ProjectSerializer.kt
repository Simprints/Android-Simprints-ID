package com.simprints.infra.config.local.models

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

@Suppress("BlockingMethodInNonBlockingContext")
internal object ProjectSerializer : Serializer<ProtoProject> {
    override val defaultValue: ProtoProject = ProtoProject.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ProtoProject {
        try {
            return ProtoProject.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: ProtoProject, output: OutputStream) = t.writeTo(output)
}
