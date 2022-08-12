package com.simprints.infra.security.keyprovider

import android.content.SharedPreferences
import android.util.Base64.DEFAULT
import android.util.Base64.encodeToString
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.security.exceptions.MissingLocalDatabaseKeyException
import com.simprints.infra.security.random.RandomGenerator
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKMatcherScope
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecureLocalDbKeyProviderImplTest {

    companion object {
        private const val DB_NAME = "name"
    }

    private val sharedPreferencesBuilder = mockk<EncryptedSharedPreferencesBuilder>()
    private val sharedPrefs = mockk<SharedPreferences>()
    private val randomGenerator = mockk<RandomGenerator>()
    private val editor = mockk<SharedPreferences.Editor>(relaxed = true)
    private val dbKeyProvider = SecureLocalDbKeyProviderImpl(sharedPreferencesBuilder, randomGenerator)

    @Before
    fun setup() {
        every { sharedPreferencesBuilder.buildEncryptedSharedPreferences(any()) } returns sharedPrefs
        every { sharedPrefs.edit() } returns editor
    }

    @Test
    fun `get local db key should throw an error if the key is missing`() {
        every { sharedPrefs.getString(any(), any()) } returns null

        assertThrows<MissingLocalDatabaseKeyException> {
            dbKeyProvider.getLocalDbKeyOrThrow(DB_NAME)
        }
    }

    @Test
    fun `get local db key should returns the key`() {
        val key = "aKey".toByteArray()
        every { sharedPrefs.getString(any(), any()) } returns encodeToString(key, DEFAULT)

        val receivedKey = dbKeyProvider.getLocalDbKeyOrThrow(DB_NAME)
        assertThat(receivedKey).isEqualTo(LocalDbKey(DB_NAME, key))
    }

    @Test
    fun `create local db key should generate a new key if it doesn't exist`() {
        val key = "aKey".toByteArray()

        every { randomGenerator.generateByteArray() } returns key
        every { sharedPrefs.getString(any(), any()) } returns null

        dbKeyProvider.createLocalDatabaseKeyIfMissing(DB_NAME)

        verify { editor.putString(containsString(DB_NAME), encodeToString(key, DEFAULT)) }
    }

    @Test
    fun `create local db key should not generate a new key if it exists`() {
        every { sharedPrefs.getString(any(), any()) } returns "aKey"

        dbKeyProvider.createLocalDatabaseKeyIfMissing(DB_NAME)

        verify(exactly = 0) { editor.putString(any(), any()) }
    }

    private fun MockKMatcherScope.containsString(s: String) = match<String> {
        it.contains(s)
    }
}
