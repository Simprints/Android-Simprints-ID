package com.simprints.infra.enrolment.records.store.usecases

import com.simprints.core.domain.tokenization.TokenizableString
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
        val s2 = "s2"
        val result = useCase(s1, s2, tokenKeyType, project)
        assertFalse(result)
    }

    @Test
    fun `should return true if strings are equal`() {
        val s1 = "s1"
        val s2 = s1
        val result = useCase(s1, s2, tokenKeyType, project)
        assertTrue(result)
    }

    @Test
    fun `should try to decrypt both strings if not equal`() {
        val s1 = "s1"
        val s2 = "s2"
        useCase(s1, s2, tokenKeyType, project)
        verify(exactly = 2) { tokenizationProcessor.decrypt(any(), any(), any()) }
    }

    @Test
    fun `should encrypt both strings if not already tokenized`() {
        val s1 = "s1"
        val s2 = "s2"

        every { tokenizationProcessor.decrypt(any(), any(), any()) } returns TokenizableString.Raw("some value")
        useCase(s1, s2, tokenKeyType, project)
        verify(exactly = 2) { tokenizationProcessor.encrypt(any(), any(), any()) }
    }

    @Test
    fun `should return true for equal tokenized strings`() {
        val s1 = "s1"
        val s2 = s1

        every { tokenizationProcessor.decrypt(any(), any(), any()) } returns TokenizableString.Tokenized("some value")
        val result = useCase(s1, s2, tokenKeyType, project)
        assertTrue(result)
    }

}
