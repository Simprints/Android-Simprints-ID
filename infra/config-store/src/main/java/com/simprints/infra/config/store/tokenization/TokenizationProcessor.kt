package com.simprints.infra.config.store.tokenization

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.utils.StringTokenizer
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.logging.Simber
import javax.inject.Inject

class TokenizationProcessor @Inject constructor(
    private val stringTokenizer: StringTokenizer,
) {
    /**
     * Tries to encrypt [decrypted] value in safely manner.
     *
     * @param decrypted raw string value for encryption
     * @param tokenKeyType corresponding key type of the provided string
     * @param project current project configuration containing tokenization keys
     *
     * @return [TokenizableString.Tokenized] in case of successful tokenization, [TokenizableString.Raw]
     * with the [decrypted] value otherwise
     */
    fun encrypt(
        decrypted: TokenizableString.Raw,
        tokenKeyType: TokenKeyType,
        project: Project,
    ): TokenizableString {
        val moduleKeyset = project.tokenizationKeys[tokenKeyType] ?: return decrypted
        return try {
            stringTokenizer.encrypt(decrypted.value, moduleKeyset).asTokenizableEncrypted()
        } catch (e: Exception) {
            Simber.e("Failed to encrypt tokenized value", e)
            decrypted
        }
    }

    /**
     * Tries to decrypt [encrypted] value in safely manner.
     *
     * @param encrypted tokenized string value for decryption
     * @param tokenKeyType corresponding key type of the provided string
     * @param project current project configuration containing tokenization keys
     *
     * @return [TokenizableString.Raw] in case of successful decryption, [TokenizableString.Tokenized]
     * with the original [encrypted] value otherwise
     */
    fun decrypt(
        encrypted: TokenizableString.Tokenized,
        tokenKeyType: TokenKeyType,
        project: Project,
        logError: Boolean = true
    ): TokenizableString {
        val moduleKeyset = project.tokenizationKeys[tokenKeyType] ?: return encrypted
        return try {
            stringTokenizer.decrypt(encrypted.value, moduleKeyset).asTokenizableRaw()
        } catch (e: Exception) {
            if (logError) {
                Simber.e("Failed to decrypt tokenized value", e)
            }
            encrypted
        }
    }
}
