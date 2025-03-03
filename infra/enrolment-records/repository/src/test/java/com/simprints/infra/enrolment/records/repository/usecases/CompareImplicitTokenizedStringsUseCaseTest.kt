package com.simprints.infra.enrolment.records.repository.usecases

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompareImplicitTokenizedStringsUseCaseTest {
    lateinit var useCase: CompareImplicitTokenizedStringsUseCase

    @MockK
    lateinit var tokenizationProcessor: TokenizationProcessor

    @MockK
    lateinit var project: Project

    @MockK
    lateinit var tokenKeyType: TokenKeyType

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = CompareImplicitTokenizedStringsUseCase(tokenizationProcessor)
    }

    @Test
    fun `should return false when s1 is null`() {
        val s1 = null
        val s2 = TokenizableString.Raw("s2")

        val result = useCase(s1, s2, tokenKeyType, project)

        assertFalse(result)
    }

    @Test
    fun `should return true if strings are equal (both untokenized)`() {
        val s1 = "s1".asTokenizableRaw()
        val s2 = s1
        every { tokenizationProcessor.decrypt(any(), any(), any(), any()) } returns TokenizableString.Tokenized("some value")

        val result = useCase(s1, s2, tokenKeyType, project)

        verify(exactly = 2) { tokenizationProcessor.encrypt(any(), any(), any()) }
        assertTrue(result)
    }

    @Test
    fun `should return false if strings are not equal (both untokenized)`() {
        val s1 = "s1".asTokenizableRaw()
        val s2 = "s2".asTokenizableRaw()
        every { tokenizationProcessor.decrypt(any(), any(), any(), any()) } returns TokenizableString.Tokenized("some value")

        val result = useCase(s1, s2, tokenKeyType, project)

        verify(exactly = 2) { tokenizationProcessor.encrypt(any(), any(), any()) }
        assertFalse(result)
    }

    @Test
    fun `should call encrypt if only first is tokenized)`() {
        val s1 = "s1".asTokenizableEncrypted()
        val s2 = "s2".asTokenizableRaw()
        every { tokenizationProcessor.decrypt(any(), any(), any(), any()) } returns TokenizableString.Tokenized("some value")

        useCase(s1, s2, tokenKeyType, project)

        verify(exactly = 1) { tokenizationProcessor.encrypt(any(), any(), any()) }
    }

    @Test
    fun `should call encrypt if only second is tokenized)`() {
        val s1 = "s1".asTokenizableRaw()
        val s2 = "encrypted s1".asTokenizableEncrypted()
        every { tokenizationProcessor.decrypt(any(), any(), any(), any()) } returns TokenizableString.Tokenized("some value")

        useCase(s1, s2, tokenKeyType, project)

        verify(exactly = 1) { tokenizationProcessor.encrypt(s1, any(), any()) }
    }

    @Test
    fun `should not call encrypt and return true if both strings are tokenized and equal`() {
        val s1 = "s1".asTokenizableEncrypted()
        val s2 = "s1".asTokenizableEncrypted()
        every { tokenizationProcessor.decrypt(any(), any(), any(), any()) } returns TokenizableString.Raw("some value")

        val result = useCase(s1, s2, tokenKeyType, project)

        verify(exactly = 0) { tokenizationProcessor.encrypt(any(), any(), any()) }
        assertTrue(result)
    }

    @Test
    fun `should not call encrypt and return false if both strings are tokenized but not equal`() {
        val s1 = "s1".asTokenizableEncrypted()
        val s2 = "s2".asTokenizableEncrypted()
        every { tokenizationProcessor.decrypt(any(), any(), any(), any()) } returns TokenizableString.Raw("some value")

        val result = useCase(s1, s2, tokenKeyType, project)

        verify(exactly = 0) { tokenizationProcessor.encrypt(any(), any(), any()) }
        assertFalse(result)
    }

    @Test
    fun `should return false if not equal`() {
        val s1 = "s1".asTokenizableRaw()
        val s2 = "s2".asTokenizableRaw()

        val result = useCase(s1, s2, tokenKeyType, project)

        assertFalse(result)
    }
}
