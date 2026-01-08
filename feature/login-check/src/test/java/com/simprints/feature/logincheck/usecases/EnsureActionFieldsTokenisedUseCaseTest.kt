package com.simprints.feature.logincheck.usecases

import com.google.common.truth.Truth.*
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.orchestration.data.ActionRequest
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class EnsureActionFieldsTokenisedUseCaseTest {
    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var tokenizationProcessor: TokenizationProcessor

    @MockK
    private lateinit var project: Project

    private lateinit var useCase: EnsureActionFieldsTokenizedUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = EnsureActionFieldsTokenizedUseCase(configRepository, tokenizationProcessor)

        every { tokenizationProcessor.tokenizeIfNecessary(any(), any(), any()) } returns "value".asTokenizableEncrypted()
    }

    @Test
    fun `when project is null, return original action`() = runTest {
        coEvery { configRepository.getProject() } returns null

        val expected = mockk<ActionRequest>()
        val result = useCase(expected)

        assertThat(result).isSameInstanceAs(expected)
    }

    @Test
    fun `when invoking with EnrolActionRequest, attempt tokenizing fields`() = runTest {
        coEvery { configRepository.getProject() } returns project

        val action = mockk<ActionRequest.EnrolActionRequest> {
            every { userId } returns "user".asTokenizableRaw()
            every { moduleId } returns "module".asTokenizableRaw()
        }
        every { action.copy(userId = any(), moduleId = any()) } returns action
        val result = useCase(action)

        assertThat(result).isInstanceOf(ActionRequest.EnrolActionRequest::class.java)
        verify {
            tokenizationProcessor.tokenizeIfNecessary(any(), TokenKeyType.ModuleId, any())
            tokenizationProcessor.tokenizeIfNecessary(any(), TokenKeyType.AttendantId, any())
        }
    }

    @Test
    fun `when invoking with VerifyActionRequest, attempt tokenizing fields`() = runTest {
        coEvery { configRepository.getProject() } returns project

        val action = mockk<ActionRequest.VerifyActionRequest> {
            every { userId } returns "user".asTokenizableRaw()
            every { moduleId } returns "module".asTokenizableRaw()
        }
        every { action.copy(userId = any(), moduleId = any()) } returns action
        val result = useCase(action)

        assertThat(result).isInstanceOf(ActionRequest.VerifyActionRequest::class.java)
        verify {
            tokenizationProcessor.tokenizeIfNecessary(any(), TokenKeyType.ModuleId, any())
            tokenizationProcessor.tokenizeIfNecessary(any(), TokenKeyType.AttendantId, any())
        }
    }

    @Test
    fun `when invoking with IdentifyActionRequest, attempt tokenizing fields`() = runTest {
        coEvery { configRepository.getProject() } returns project

        val action = mockk<ActionRequest.IdentifyActionRequest> {
            every { userId } returns "user".asTokenizableRaw()
            every { moduleId } returns "module".asTokenizableRaw()
        }
        every { action.copy(userId = any(), moduleId = any()) } returns action
        val result = useCase(action)

        assertThat(result).isInstanceOf(ActionRequest.IdentifyActionRequest::class.java)
        verify {
            tokenizationProcessor.tokenizeIfNecessary(any(), TokenKeyType.ModuleId, any())
            tokenizationProcessor.tokenizeIfNecessary(any(), TokenKeyType.AttendantId, any())
        }
    }

    @Test
    fun `when invoking with ConfirmIdentityActionRequest, attempt tokenizing fields`() = runTest {
        coEvery { configRepository.getProject() } returns project

        val action = mockk<ActionRequest.ConfirmIdentityActionRequest> {
            every { userId } returns "user".asTokenizableRaw()
        }
        every { action.copy(userId = any()) } returns action
        val result = useCase(action)

        assertThat(result).isInstanceOf(ActionRequest.ConfirmIdentityActionRequest::class.java)
        verify {
            tokenizationProcessor.tokenizeIfNecessary(any(), TokenKeyType.AttendantId, any())
        }
    }

    @Test
    fun `when invoking with EnrolLastBiometricActionRequest, attempt tokenizing fields`() = runTest {
        coEvery { configRepository.getProject() } returns project

        val action = mockk<ActionRequest.EnrolLastBiometricActionRequest> {
            every { userId } returns "user".asTokenizableRaw()
            every { moduleId } returns "module".asTokenizableRaw()
        }
        every { action.copy(userId = any(), moduleId = any()) } returns action
        val result = useCase(action)

        assertThat(result).isInstanceOf(ActionRequest.EnrolLastBiometricActionRequest::class.java)
        verify {
            tokenizationProcessor.tokenizeIfNecessary(any(), TokenKeyType.ModuleId, any())
            tokenizationProcessor.tokenizeIfNecessary(any(), TokenKeyType.AttendantId, any())
        }
    }
}
