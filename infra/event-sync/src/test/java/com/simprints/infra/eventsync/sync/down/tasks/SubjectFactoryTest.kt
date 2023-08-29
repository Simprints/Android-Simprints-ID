package com.simprints.infra.eventsync.sync.down.tasks

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.core.tools.utils.Tokenization
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.TokenKeyType
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test


class SubjectFactoryTest {

    @MockK
    lateinit var encodingUtils: EncodingUtils

    @MockK
    lateinit var tokenization: Tokenization

    @MockK
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var project: Project

    private lateinit var factory: SubjectFactory
    private val projectId = "projectId"
    private val keysetJson = "keysetJson"
    private val subjectId = "subjectId"
    private val attendantId = "encryptedAttendantId"
    private val moduleId = "encryptedModuleId"

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        factory = SubjectFactory(
            encodingUtils = encodingUtils,
            tokenization = tokenization,
            configManager = configManager
        )
    }

    @Test
    fun `given attendant id key is presented in tokenization map, when encrypted subject is built, then attendant id is encrypted`() {
        runEncryptionTest(
            tokenKeyType = TokenKeyType.AttendantId,
            value = attendantId,
            expectedEncrypted = "encryptedAttendantId"
        )
    }

    @Test
    fun `given module id key is presented in tokenization map, when encrypted subject is built, then module id is encrypted`() {
        runEncryptionTest(
            tokenKeyType = TokenKeyType.ModuleId,
            value = moduleId,
            expectedEncrypted = "encryptedModuleId"
        )
    }

    @Test
    fun `when encryption fails, then values are not encrypted`() = runTest {
        every { project.tokenizationKeys } returns mapOf(
            TokenKeyType.ModuleId to keysetJson,
            TokenKeyType.AttendantId to keysetJson,
        )
        coEvery { configManager.getProject(projectId) } returns project
        coEvery { tokenization.encrypt(any(), any()) } throws SecurityException()

        val subject = factory.buildEncryptedSubject(
            subjectId = subjectId,
            projectId = projectId,
            attendantId = attendantId,
            moduleId = moduleId
        )
        assertThat(subject.attendantId).isEqualTo(attendantId)
        assertThat(subject.moduleId).isEqualTo(moduleId)
        verify(exactly = 2) { tokenization.encrypt(any(), any()) }
    }

    private fun runEncryptionTest(
        tokenKeyType: TokenKeyType,
        value: String,
        expectedEncrypted: String
    ) {
        runTest {
            every { project.tokenizationKeys } returns mapOf(tokenKeyType to keysetJson)
            coEvery { configManager.getProject(projectId) } returns project
            coEvery { tokenization.encrypt(value, keysetJson) } returns expectedEncrypted

            val subject = factory.buildEncryptedSubject(
                subjectId = subjectId,
                projectId = projectId,
                attendantId = attendantId,
                moduleId = moduleId
            )

            val result = when (tokenKeyType) {
                TokenKeyType.AttendantId -> subject.attendantId
                TokenKeyType.ModuleId -> subject.moduleId
                TokenKeyType.Unknown -> throw Exception("Incorrect token key type provided")
            }
            assertThat(result).isEqualTo(expectedEncrypted)
            verify(exactly = 1) { tokenization.encrypt(value, keysetJson) }
        }
    }

}