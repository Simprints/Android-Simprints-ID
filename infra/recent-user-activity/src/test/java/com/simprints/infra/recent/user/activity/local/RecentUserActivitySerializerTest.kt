package com.simprints.infra.recent.user.activity.local

import androidx.datastore.core.CorruptionException
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.recent.user.activity.ProtoRecentUserActivity
import com.simprints.testtools.common.syntax.assertThrows
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class RecentUserActivitySerializerTest {
    companion object {
        private val protoRecentUserActivity = ProtoRecentUserActivity
            .newBuilder()
            .setLastScannerVersion("version")
            .setLastScannerUsed("scanner")
            .setLastUserUsed("user")
            .setEnrolmentsToday(10)
            .setIdentificationsToday(20)
            .setVerificationsToday(30)
            .setLastActivityTime(50L)
            .build()
    }

    @Test
    fun `readFrom should return the ProtoRecentUserActivity when it's valid proto`() = runTest {
        val outputStream = ByteArrayOutputStream()
        RecentUserActivitySerializer.writeTo(protoRecentUserActivity, outputStream)
        val project =
            RecentUserActivitySerializer.readFrom(ByteArrayInputStream(outputStream.toByteArray()))
        assertThat(project).isEqualTo(protoRecentUserActivity)
    }

    @Test
    fun `readFrom should throw a CorruptionException when the proto is invalid`() = runTest {
        assertThrows<CorruptionException> {
            RecentUserActivitySerializer.readFrom(ByteArrayInputStream("invalid".toByteArray()))
        }
    }
}
