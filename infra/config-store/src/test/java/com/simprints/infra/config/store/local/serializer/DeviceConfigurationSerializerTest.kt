package com.simprints.infra.config.store.local.serializer

import androidx.datastore.core.CorruptionException
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.testtools.protoDeviceConfiguration
import com.simprints.testtools.common.syntax.assertThrows
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class DeviceConfigurationSerializerTest {
    @Test
    fun `readFrom should return the ProtoDeviceConfiguration when it's valid proto`() = runTest {
        val outputStream = ByteArrayOutputStream()
        DeviceConfigurationSerializer.writeTo(protoDeviceConfiguration, outputStream)
        val project =
            DeviceConfigurationSerializer.readFrom(ByteArrayInputStream(outputStream.toByteArray()))
        assertThat(project).isEqualTo(protoDeviceConfiguration)
    }

    @Test
    fun `readFrom should throw a CorruptionException when the proto is invalid`() = runTest {
        assertThrows<CorruptionException> {
            DeviceConfigurationSerializer.readFrom(ByteArrayInputStream("invalid".toByteArray()))
        }
    }
}
