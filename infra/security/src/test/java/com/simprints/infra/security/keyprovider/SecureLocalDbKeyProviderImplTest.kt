package com.simprints.infra.security.keyprovider

import android.content.SharedPreferences
import android.util.Base64.DEFAULT
import android.util.Base64.encodeToString
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.exceptions.MatchingLocalDatabaseKeyHashesException
import com.simprints.infra.security.exceptions.MismatchingLocalDatabaseKeyHashesException
import com.simprints.infra.security.exceptions.MissingLocalDatabaseKeyException
import com.simprints.infra.security.exceptions.MissingLocalDatabaseKeyHashException
import com.simprints.infra.security.random.RandomGenerator
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecureLocalDbKeyProviderImplTest {
    companion object {
        private const val DB_NAME = "name"
        private const val KEY_NAME = "REALM_KEY_$DB_NAME"
    }

    private val sharedPreferencesBuilder = mockk<EncryptedSharedPreferencesBuilder>()
    private val dbKeySharedPrefs = mockk<SharedPreferences>()
    private val hashSharedPrefs = mockk<SharedPreferences>()
    private val randomGenerator = mockk<RandomGenerator> {
        every { generateByteArray() } returns byteArrayOf(1, 2, 3)
    }
    private val dbKeyEditor = mockk<SharedPreferences.Editor>(relaxed = true)
    private val keyHashEditor = mockk<SharedPreferences.Editor>(relaxed = true)
    private val dbKeyProvider =
        SecureLocalDbKeyProviderImpl(sharedPreferencesBuilder, randomGenerator)

    @Before
    fun setup() {
        every {
            sharedPreferencesBuilder.buildEncryptedSharedPreferences(SecureLocalDbKeyProvider.FILENAME_FOR_REALM_KEY_SHARED_PREFS)
        } returns dbKeySharedPrefs
        every { dbKeySharedPrefs.edit() } returns dbKeyEditor
        every {
            sharedPreferencesBuilder.buildEncryptedSharedPreferences(SecureLocalDbKeyProvider.FILENAME_FOR_KEY_HASHES_SHARED_PREFS)
        } returns hashSharedPrefs
        every { hashSharedPrefs.edit() } returns keyHashEditor
        mockkObject(Simber)
    }

    @Test
    fun `get local db key should throw an error if the key is missing`() {
        every { dbKeySharedPrefs.getString(any(), any()) } returns null

        assertThrows<MissingLocalDatabaseKeyException> {
            dbKeyProvider.getLocalDbKeyOrThrow(DB_NAME)
        }
    }

    @Test
    fun `get local db key should returns the key`() {
        val key = "aKey".toByteArray()
        every { dbKeySharedPrefs.getString(KEY_NAME, null) } returns encodeToString(key, DEFAULT)
        every { hashSharedPrefs.getString(KEY_NAME, null) } returns null

        val receivedKey = dbKeyProvider.getLocalDbKeyOrThrow(DB_NAME)
        verify { keyHashEditor.putString(KEY_NAME, any()) }
        assertThat(receivedKey).isEqualTo(LocalDbKey(DB_NAME, key))
    }

    @Test
    fun `create local db key should generate a new key if it doesn't exist`() {
        val key = "aKey".toByteArray()

        every { randomGenerator.generateByteArray() } returns key
        every { dbKeySharedPrefs.getString(any(), any()) } returns null

        dbKeyProvider.createLocalDatabaseKeyIfMissing(DB_NAME)

        verify { dbKeyEditor.putString(containsString(DB_NAME), encodeToString(key, DEFAULT)) }
    }

    @Test
    fun `create local db key should not generate a new key if it exists`() {
        every { dbKeySharedPrefs.getString(any(), any()) } returns "aKey"

        dbKeyProvider.createLocalDatabaseKeyIfMissing(DB_NAME)

        verify(exactly = 0) { dbKeyEditor.putString(any(), any()) }
    }

    @Test
    fun `test recreate db key will log MissingLocalDatabaseKeyHashException if there is no hash stored`() {
        every { dbKeySharedPrefs.getString(KEY_NAME, null) } returns "aKey"
        every { hashSharedPrefs.getString(KEY_NAME, null) } returns null

        dbKeyProvider.recreateLocalDatabaseKey(DB_NAME)

        verify {
            dbKeyEditor.putString(KEY_NAME, any())
            keyHashEditor.putString(KEY_NAME, any())
            Simber.i(any(), ofType<MissingLocalDatabaseKeyHashException>())
        }
    }

    @Test
    fun `test recreate db key will log MatchingLocalDatabaseKeyHashesException if the same hash is stored`() {
        every { dbKeySharedPrefs.getString(KEY_NAME, null) } returns "name"
        every {
            hashSharedPrefs.getString(KEY_NAME, null)
        } returns
            "b114f311db0e009ca2a88a9b97b1d7b362ddb27dc3dd214c6d20327a1fc3add8cc488cca4cc3565a876f6040f8b73a7b92475be1d0b1bc453f6140fba7183b9a"

        dbKeyProvider.recreateLocalDatabaseKey(DB_NAME)

        verify {
            dbKeyEditor.putString(KEY_NAME, any())
            keyHashEditor.putString(KEY_NAME, any())
            Simber.i(any(), ofType<MatchingLocalDatabaseKeyHashesException>())
        }
    }

    @Test
    fun `test recreate db key will log MismatchingLocalDatabaseKeyHashesException if the same hash stored`() {
        every { dbKeySharedPrefs.getString(KEY_NAME, null) } returns "key"
        every { hashSharedPrefs.getString(KEY_NAME, null) } returns "hash"

        dbKeyProvider.recreateLocalDatabaseKey(DB_NAME)

        verify {
            dbKeyEditor.putString(KEY_NAME, any())
            keyHashEditor.putString(KEY_NAME, any())
            Simber.i(any(), ofType<MismatchingLocalDatabaseKeyHashesException>())
        }
    }

    @Test
    fun `test recreate db key will log MissingLocalDatabaseKeyException if no key stored`() {
        every { dbKeySharedPrefs.getString(KEY_NAME, null) } returns null

        dbKeyProvider.recreateLocalDatabaseKey(DB_NAME)

        verify {
            dbKeyEditor.putString(KEY_NAME, any())
            keyHashEditor.putString(KEY_NAME, any())
            Simber.i(any(), ofType<MissingLocalDatabaseKeyException>())
        }
    }

    private fun MockKMatcherScope.containsString(s: String) = match<String> {
        it.contains(s)
    }
}
