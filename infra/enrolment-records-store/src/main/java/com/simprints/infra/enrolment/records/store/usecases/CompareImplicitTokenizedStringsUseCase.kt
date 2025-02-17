package com.simprints.infra.enrolment.records.store.usecases

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import javax.inject.Inject

/**
 * Use case that checks two plain strings values but considers that the tokenization state of one of the values
 * might differ from another. In this case this use tries to bring both strings to the same tokenization state
 * and compare their values.
 * Example:
 *      s1 = 'abc'                  - untokenized value
 *      s2 = 'AWcDe/==seF1LkcF4'    - tokenized value (result of tokenizing the 'abc' value)
 *
 *      Even though plain string values are different, they represent the same entity. s1 is going to be encrypted and compared to the s2.
 */
class CompareImplicitTokenizedStringsUseCase @Inject constructor(
    private val tokenizationProcessor: TokenizationProcessor,
) {
    operator fun invoke(
        s1: TokenizableString?,
        s2: String,
        tokenKeyType: TokenKeyType,
        project: Project,
    ): Boolean = when {
        s1 == null -> false
        else -> ensureTokenized(s1, tokenKeyType, project) == ensureTokenized(s2, tokenKeyType, project)
    }

    private fun ensureTokenized(
        s: TokenizableString,
        tokenKeyType: TokenKeyType,
        project: Project,
    ): TokenizableString = when (s) {
        is TokenizableString.Tokenized -> s
        is TokenizableString.Raw -> tokenizationProcessor.encrypt(
            decrypted = s,
            tokenKeyType = tokenKeyType,
            project = project,
        )
    }

    private fun ensureTokenized(
        s: String,
        tokenKeyType: TokenKeyType,
        project: Project,
    ): TokenizableString {
        val isAlreadyTokenized = tokenizationProcessor.decrypt(
            encrypted = s.asTokenizableEncrypted(),
            tokenKeyType = tokenKeyType,
            project = project,
            logError = false,
        ) is TokenizableString.Raw

        return if (isAlreadyTokenized) {
            s.asTokenizableEncrypted()
        } else {
            tokenizationProcessor.encrypt(
                decrypted = s.asTokenizableRaw(),
                tokenKeyType = tokenKeyType,
                project = project,
            )
        }
    }
}
