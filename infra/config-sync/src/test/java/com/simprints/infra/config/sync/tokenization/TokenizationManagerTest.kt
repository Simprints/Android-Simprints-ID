package com.simprints.infra.config.sync.tokenization

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizedEncrypted
import com.simprints.core.domain.tokenization.asTokenizedRaw
import com.simprints.core.tools.utils.StringTokenizer
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import io.mockk.every
import io.mockk.mockk
import org.junit.Test


class TokenizationManagerTest {

    private val raw = "raw"
    private val encrypted = "encrypted"
    private val keySet = "keyset"
    private val tokenKeyType = TokenKeyType.ModuleId
    private val project = mockk<Project> {
        every { tokenizationKeys[any()] } returns keySet
    }
    private val stringTokenizer: StringTokenizer = mockk {
        every { encrypt(raw, keySet) } returns encrypted
        every { decrypt(encrypted, keySet) } returns raw
    }

    private val manager = TokenizationManager(stringTokenizer)

    @Test
    fun `when tokenization key is presented, should encrypt value`() {
        val result = manager.encrypt(
            decrypted = raw.asTokenizedRaw(),
            tokenKeyType = tokenKeyType,
            project = project
        )
        assertThat(result).isEqualTo(encrypted.asTokenizedEncrypted())
    }
    @Test
    fun `when tokenization key is not presented, should not encrypt value`() {
        every { project.tokenizationKeys } returns emptyMap()
        val decrypted = raw.asTokenizedRaw()
        val result = manager.encrypt(
            decrypted = decrypted,
            tokenKeyType = tokenKeyType,
            project = project
        )
        assertThat(result).isEqualTo(decrypted)
    }
    @Test
    fun `when encryption throws exception, should return unencrypted value`() {
        every { stringTokenizer.encrypt(any(),any()) } throws Exception()
        val decrypted = raw.asTokenizedRaw()
        val result = manager.encrypt(
            decrypted = decrypted,
            tokenKeyType = tokenKeyType,
            project = project
        )
        assertThat(result).isEqualTo(decrypted)
    }
    @Test
    fun `when tokenization key is presented, should decrypt value`() {
        val result = manager.decrypt(
            encrypted = raw.asTokenizedEncrypted(),
            tokenKeyType = tokenKeyType,
            project = project
        )
        assertThat(result).isEqualTo(raw.asTokenizedRaw())
    }
    @Test
    fun `when tokenization key is not presented, should not decrypt value`() {
        every { project.tokenizationKeys } returns emptyMap()
        val encrypted = raw.asTokenizedEncrypted()
        val result = manager.decrypt(
            encrypted = encrypted,
            tokenKeyType = tokenKeyType,
            project = project
        )
        assertThat(result).isEqualTo(encrypted)
    }
    @Test
    fun `when decryption throws exception, should return encrypted value`() {
        every { stringTokenizer.encrypt(any(),any()) } throws Exception()
        val encrypted = raw.asTokenizedEncrypted()
        val result = manager.decrypt(
            encrypted = encrypted,
            tokenKeyType = tokenKeyType,
            project = project
        )
        assertThat(result).isEqualTo(encrypted)
    }
}
