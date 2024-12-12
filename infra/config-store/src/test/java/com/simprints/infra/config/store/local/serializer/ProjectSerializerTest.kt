package com.simprints.infra.config.store.local.serializer

import androidx.datastore.core.CorruptionException
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.testtools.protoProject
import com.simprints.testtools.common.syntax.assertThrows
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ProjectSerializerTest {
    @Test
    fun `readFrom should return the ProtoProject when it's valid proto`() = runTest {
        val outputStream = ByteArrayOutputStream()
        ProjectSerializer.writeTo(protoProject, outputStream)
        val project = ProjectSerializer.readFrom(ByteArrayInputStream(outputStream.toByteArray()))
        assertThat(project).isEqualTo(protoProject)
    }

    @Test
    fun `readFrom should throw a CorruptionException when the proto is invalid`() = runTest {
        assertThrows<CorruptionException> {
            ProjectSerializer.readFrom(ByteArrayInputStream("invalid".toByteArray()))
        }
    }
}
