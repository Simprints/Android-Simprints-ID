package com.simprints.infra.recent.user.activity.local

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.simprints.infra.recent.user.activity.ProtoRecentUserActivity
import java.io.InputStream
import java.io.OutputStream

internal object RecentUserActivitySerializer : Serializer<ProtoRecentUserActivity> {
    override val defaultValue: ProtoRecentUserActivity =
        ProtoRecentUserActivity.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ProtoRecentUserActivity {
        try {
            return ProtoRecentUserActivity.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: ProtoRecentUserActivity,
        output: OutputStream,
    ) = t.writeTo(output)
}
