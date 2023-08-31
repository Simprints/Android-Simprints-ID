package com.simprints.infra.config.tokenization

import com.simprints.core.tools.utils.Tokenization
import com.simprints.infra.config.domain.TokenizationAction
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.logging.Simber
import javax.inject.Inject

class TokenizationManager @Inject constructor(
    private val tokenization: Tokenization
) {

    /**
     * Performs tokenization action on the provided [value] in safely manner. If encryption or decryption
     * fails, then returns the initial [value].
     *
     * @param value string for tokenization action
     * @param tokenKeyType corresponding key type of the provided string
     * @param action defines what to do with the string:
     *  - [TokenizationAction.Encrypt] for encryption
     *  - [TokenizationAction.Decrypt] for decryption
     * @param project current project configuration containing tokenization keys
     */
    fun tryTokenize(
        value: String,
        tokenKeyType: TokenKeyType,
        action: TokenizationAction,
        project: Project
    ): String {
        val moduleKeyset = project.tokenizationKeys[tokenKeyType] ?: return value
        return try {
            when (action) {
                TokenizationAction.Encrypt -> tokenization.encrypt(value, moduleKeyset)
                TokenizationAction.Decrypt -> tokenization.decrypt(value, moduleKeyset)
            }
        } catch (e: Exception) {
            Simber.e(e)
            value
        }
    }
}