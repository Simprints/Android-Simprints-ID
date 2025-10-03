package com.simprints.feature.externalcredential.screens.search.usecase

import com.google.common.truth.Truth.*
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class FindSubjectsByCredentialUseCaseTest {

    private lateinit var useCase: FindSubjectsByCredentialUseCase

    @MockK
    private lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    private lateinit var tokenizationProcessor: TokenizationProcessor

    @MockK
    private lateinit var subject: Subject

    @MockK
    private lateinit var project: Project

    private val credentialRaw = "credentialRaw"
    private val credentialTokenized = TokenizableString.Tokenized("credentialTokenized")
    private val projectId = "projectId"

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = FindSubjectsByCredentialUseCase(
            enrolmentRecordRepository = enrolmentRecordRepository,
            tokenizationProcessor = tokenizationProcessor
        )

        every { project.id } returns projectId
        every { tokenizationProcessor.encrypt(decrypted = any(), tokenKeyType = any(), project = any()) } returns credentialTokenized
        coEvery {
            enrolmentRecordRepository.load(
                SubjectQuery(
                    projectId = projectId,
                    externalCredential = credentialTokenized
                )
            )
        } returns listOf(subject)
    }

    @Test
    fun `encrypts credential and loads subjects successfully`() = runTest {
        val result = useCase.invoke(credentialRaw, project)
        assertThat(result).containsExactly(subject)
        coVerify(exactly = 1) {
            tokenizationProcessor.encrypt(
                decrypted = credentialRaw.asTokenizableRaw(),
                tokenKeyType = TokenKeyType.ExternalCredential,
                project = project
            )
        }
        coVerify(exactly = 1) {
            enrolmentRecordRepository.load(
                SubjectQuery(
                    projectId = projectId,
                    externalCredential = credentialTokenized
                )
            )
        }
    }

    @Test
    fun `returns empty list when no subjects found`() = runTest {
        coEvery { enrolmentRecordRepository.load(any()) } returns emptyList()
        val result = useCase.invoke(credentialRaw, project)
        assertThat(result).isEmpty()
    }

    @Test
    fun `uses correct token key type for external credential`() = runTest {
        useCase.invoke(credentialRaw, project)
        coVerify {
            tokenizationProcessor.encrypt(
                decrypted = any(),
                tokenKeyType = TokenKeyType.ExternalCredential,
                project = any()
            )
        }
    }

    @Test
    fun `creates subject query with correct parameters`() = runTest {
        useCase.invoke(credentialRaw, project)
        coVerify {
            enrolmentRecordRepository.load(
                SubjectQuery(
                    projectId = projectId,
                    externalCredential = credentialTokenized
                )
            )
        }
    }

    @Test
    fun `throws ClassCastException when tokenization fails`() = runTest {
        val unencrypted = "unencrypted".asTokenizableRaw()
        every {
            tokenizationProcessor.encrypt(
                decrypted = any(),
                tokenKeyType = any(),
                project = any()
            )
        } returns unencrypted

        assertThrows<ClassCastException> {
            useCase.invoke(credentialRaw, project)
        }
    }
}
