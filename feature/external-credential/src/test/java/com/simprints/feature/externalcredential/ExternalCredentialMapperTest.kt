package com.simprints.feature.externalcredential

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredentialResult
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class ExternalCredentialMapperTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var tokenizationProcessor: TokenizationProcessor

    @MockK
    lateinit var configRepository: ConfigRepository

    @MockK
    lateinit var project: Project

    @MockK
    lateinit var searchResult: ExternalCredentialSearchResult.Complete

    @MockK
    lateinit var scannedCredentialResult: ScannedCredentialResult

    @MockK
    lateinit var confirmedCredential: TokenizableString.Raw

    @MockK
    lateinit var encryptedCredential: TokenizableString.Tokenized

    private lateinit var mapper: ExternalCredentialMapper

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        coEvery { configRepository.getProject() } returns project
        every { searchResult.scannedCredentialResult } returns scannedCredentialResult
        every { searchResult.confirmedCredential } returns confirmedCredential
        every { scannedCredentialResult.credentialScanId } returns SCAN_ID
        every { scannedCredentialResult.credentialType } returns ExternalCredentialType.NHISCard
        every { tokenizationProcessor.encrypt(confirmedCredential, TokenKeyType.ExternalCredential, project) } returns encryptedCredential

        mapper = ExternalCredentialMapper(tokenizationProcessor, configRepository)
    }

    @Test
    fun `maps credential scan id to external credential id`() = runTest {
        val result = mapCredential()
        assertThat(result.id).isEqualTo(SCAN_ID)
    }

    @Test
    fun `maps encrypted confirmed credential to external credential value`() = runTest {
        val result = mapCredential()
        assertThat(result.value).isEqualTo(encryptedCredential)
    }

    @Test
    fun `passes subject id through to external credential`() = runTest {
        val result = mapCredential(subjectId = SUBJECT_ID)
        assertThat(result.subjectId).isEqualTo(SUBJECT_ID)
    }

    @Test
    fun `maps credential type from scanned result`() = runTest {
        every { scannedCredentialResult.credentialType } returns ExternalCredentialType.GhanaIdCard
        val result = mapCredential()
        assertThat(result.type).isEqualTo(ExternalCredentialType.GhanaIdCard)
    }

    @Test
    fun `encrypts confirmed credential using external credential key type`() = runTest {
        mapCredential()
        coVerify {
            tokenizationProcessor.encrypt(
                decrypted = confirmedCredential,
                tokenKeyType = TokenKeyType.ExternalCredential,
                project = project,
            )
        }
    }

    @Test
    fun `fetches project from config repository for encryption`() = runTest {
        mapCredential()
        coVerify { configRepository.getProject() }
    }

    private suspend fun mapCredential(subjectId: String = SUBJECT_ID) =
        mapper.mapExternalCredential(searchResult = searchResult, subjectId = subjectId)

    companion object {
        private const val SCAN_ID = "SCAN_ID"
        private const val SUBJECT_ID = "SUBJECT_ID"
    }
}
