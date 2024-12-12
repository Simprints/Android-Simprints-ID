package com.simprints.infra.config.store.local.serializer

import androidx.datastore.core.CorruptionException
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.testtools.protoProjectConfiguration
import com.simprints.testtools.common.syntax.assertThrows
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ProjectConfigurationSerializerTest {
    @Test
    fun `readFrom should return the ProtoProjectConfiguration when it's valid proto`() = runTest {
        val outputStream = ByteArrayOutputStream()
        ProjectConfigurationSerializer.writeTo(protoProjectConfiguration, outputStream)
        val project =
            ProjectConfigurationSerializer.readFrom(ByteArrayInputStream(outputStream.toByteArray()))
        assertThat(project).isEqualTo(protoProjectConfiguration)
    }

    @Test
    fun `readFrom should throw a CorruptionException when the proto is invalid`() = runTest {
        assertThrows<CorruptionException> {
            ProjectConfigurationSerializer.readFrom(ByteArrayInputStream("invalid".toByteArray()))
        }
    }
}
