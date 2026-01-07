package com.simprints.infra.sync.config.usecase

import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.sync.config.testtools.project
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class TokenizeRecordsIfKeysChangedUseCaseTest {
    @MockK
    private lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    private lateinit var useCase: TokenizeRecordsIfKeysChangedUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = TokenizeRecordsIfKeysChangedUseCase(
            enrolmentRecordRepository = enrolmentRecordRepository,
        )
    }

    @Test
    fun `should not reset when project already had tokenization keys`() = runTest {
        useCase(
            project.copy(tokenizationKeys = mapOf(TokenKeyType.ModuleId to "token")),
            project.copy(tokenizationKeys = mapOf(TokenKeyType.ModuleId to "token")),
        )

        coVerify(exactly = 0) {
            enrolmentRecordRepository.tokenizeExistingRecords(any())
        }
    }

    @Test
    fun `should not reset when new project does not have tokenization keys`() = runTest {
        useCase(
            project.copy(tokenizationKeys = emptyMap()),
            project.copy(tokenizationKeys = emptyMap()),
        )

        coVerify(exactly = 0) {
            enrolmentRecordRepository.tokenizeExistingRecords(any())
        }
    }

    @Test
    fun `should reset when old project is not present`() = runTest {
        useCase(
            null,
            project.copy(tokenizationKeys = mapOf(TokenKeyType.ModuleId to "token")),
        )

        coVerify(exactly = 1) {
            enrolmentRecordRepository.tokenizeExistingRecords(any())
        }
    }

    @Test
    fun `should reset when project did not have tokenization keys`() = runTest {
        useCase(
            project.copy(tokenizationKeys = emptyMap()),
            project.copy(tokenizationKeys = mapOf(TokenKeyType.ModuleId to "token")),
        )

        coVerify(exactly = 1) {
            enrolmentRecordRepository.tokenizeExistingRecords(any())
        }
    }
}
