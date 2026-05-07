package com.simprints.feature.externalcredential

import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import javax.inject.Inject

class ExternalCredentialMapper @Inject constructor(
    private val tokenizationProcessor: TokenizationProcessor,
    private val configRepository: ConfigRepository,
) {
    suspend fun mapExternalCredential(
        searchResult: ExternalCredentialSearchResult.Complete,
        subjectId: String,
    ): ExternalCredential {
        val scannedCredentialResult = searchResult.scannedCredentialResult
        val confirmedCredential = searchResult.confirmedCredential
        val encrypted = tokenizationProcessor.encrypt(
            decrypted = confirmedCredential,
            tokenKeyType = TokenKeyType.ExternalCredential,
            project = configRepository.getProject()!!,
        ) as TokenizableString.Tokenized
        return ExternalCredential(
            id = scannedCredentialResult.credentialScanId,
            value = encrypted,
            subjectId = subjectId,
            type = scannedCredentialResult.credentialType,
        )
    }
}
