package com.simprints.infra.config.tokenization

import com.simprints.core.domain.tokenization.TokenizedString
import com.simprints.core.domain.tokenization.asTokenizedEncrypted
import com.simprints.core.domain.tokenization.asTokenizedRaw
import com.simprints.core.tools.utils.Tokenization
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.logging.Simber
import javax.inject.Inject

class TokenizationManager @Inject constructor(
    private val tokenization: Tokenization
) {

    /**
     * Tries to encrypt [decrypted] value in safely manner.
     *
     * @param decrypted raw string value for encryption
     * @param tokenKeyType corresponding key type of the provided string
     * @param project current project configuration containing tokenization keys
     *
     * @return [TokenizedString.Encrypted] is case of successful tokenization, [TokenizedString.Raw]
     * with the [decrypted] value otherwise
     */
    fun encrypt(
        decrypted: TokenizedString,
        tokenKeyType: TokenKeyType,
        project: Project
    ): TokenizedString {
        val moduleKeyset = project.tokenizationKeys[tokenKeyType] ?: return decrypted
        return try {
            tokenization.encrypt(decrypted.value, moduleKeyset).asTokenizedEncrypted()
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
     * @return [TokenizedString.Encrypted] is case of successful tokenization, [TokenizedString.Raw]
     * with the [encrypted] value otherwise
     */
    fun decrypt(
        encrypted: TokenizedString,
        tokenKeyType: TokenKeyType,
        project: Project
    ): TokenizedString {
        val moduleKeyset = project.tokenizationKeys[tokenKeyType] ?: return encrypted
        return try {
            tokenization.decrypt(encrypted.value, moduleKeyset).asTokenizedRaw()
        } catch (e: Exception) {
            Simber.e(e)
            encrypted
        }
    }
}