package com.simprints.infra.config.sync.tokenization

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizedEncrypted
import com.simprints.core.domain.tokenization.asTokenizedRaw
import com.simprints.core.tools.utils.StringTokenizer
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.logging.Simber
import javax.inject.Inject

class TokenizationManager @Inject constructor(
    private val stringTokenizer: StringTokenizer
) {

    /**
     * Tries to encrypt [decrypted] value in safely manner.
     *
     * @param decrypted raw string value for encryption
     * @param tokenKeyType corresponding key type of the provided string
     * @param project current project configuration containing tokenization keys
     *
     * @return [TokenizableString.Tokenized] is case of successful tokenization, [TokenizableString.Raw]
     * with the [decrypted] value otherwise
     */
    fun encrypt(
        decrypted: TokenizableString.Raw,
        tokenKeyType: TokenKeyType,
        project: Project
    ): TokenizableString {
        val moduleKeyset = project.tokenizationKeys[tokenKeyType] ?: return decrypted
        return try {
            stringTokenizer.encrypt(decrypted.value, moduleKeyset).asTokenizedEncrypted()
        } catch (e: Exception) {
            Simber.e(e)
            decrypted
        }
    }

    /**
     * Tries to decrypt [encrypted] value in safely manner.
     *
     * @param encrypted raw string value for encryption
     * @param tokenKeyType corresponding key type of the provided string
     * @param project current project configuration containing tokenization keys
     *
     * @return [TokenizableString.Tokenized] is case of successful tokenization, [TokenizableString.Raw]
     * with the [encrypted] value otherwise
     */
    fun decrypt(
        encrypted: TokenizableString.Tokenized,
        tokenKeyType: TokenKeyType,
        project: Project
    ): TokenizableString {
        val moduleKeyset = project.tokenizationKeys[tokenKeyType] ?: return encrypted
        return try {
            stringTokenizer.decrypt(encrypted.value, moduleKeyset).asTokenizedRaw()
        } catch (e: Exception) {
            Simber.e(e)
            encrypted
        }
    }
}