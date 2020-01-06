package com.simprints.id.data.secure

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64.DEFAULT
import android.util.Base64.encodeToString
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_REALM_KEY
import com.simprints.id.exceptions.safe.secure.MissingLocalDatabaseKeyException
import com.simprints.id.tools.RandomGenerator
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class SecureLocalDbKeyProviderImplTest {

    companion object {
        const val SHARED_PREFS_FOR_TEST = "test"
        private const val SHARED_PREFS_KEY_FOR_REALM = "REALM_KEY_${DEFAULT_PROJECT_ID}"
    }

    private val ctx = ApplicationProvider.getApplicationContext<Context>()
    private val sharedPrefs = ctx.getSharedPreferences(SHARED_PREFS_FOR_TEST, 0)

    private val legacyLocalDbKeyProviderMock = mockk<LegacyLocalDbKeyProvider>()
    private val randomGenerator = mockk<RandomGenerator>().apply {
        every { this@apply.generateByteArray(any()) } returns DEFAULT_REALM_KEY
    }

    private val realmKeyStored: String?
        get() = sharedPrefs.getString(SHARED_PREFS_KEY_FOR_REALM, "")

    private lateinit var secureLocalDbKeyProvider: SecureLocalDbKeyProvider

    @Before
    fun setUp() {
        secureLocalDbKeyProvider = SecureLocalDbKeyProviderImpl(sharedPrefs, randomGenerator, legacyLocalDbKeyProviderMock)
        sharedPrefs.edit().clear().apply()
    }

    @Test
    fun setLocalDatabaseKey_shouldCreateANewLocalKeyIfNotPresent() {
        assertThat(realmKeyStored).isEmpty()

        secureLocalDbKeyProvider.setLocalDatabaseKey(DEFAULT_PROJECT_ID)

        assertThat(realmKeyStored).isNotEmpty()
    }

    @Test
    fun setLocalDatabaseKey_shouldReturnTheExistingKeyIfPresent() {
        val sharedPrefsMock = mockStoredRealmKeyInSharedPrefs()
        secureLocalDbKeyProvider = SecureLocalDbKeyProviderImpl(sharedPrefsMock, randomGenerator, mockk())

        secureLocalDbKeyProvider.setLocalDatabaseKey(DEFAULT_PROJECT_ID)

        verify(exactly = 0) { sharedPrefsMock.edit() }
    }

    @Test
    fun getLocalDbKeyOrThrow_shouldReturnLocalKeyIfPresent() {
        sharedPrefs.edit().putString(SHARED_PREFS_KEY_FOR_REALM, encodeToString(DEFAULT_REALM_KEY, DEFAULT)).apply()

        val localDbKey = secureLocalDbKeyProvider.getLocalDbKeyOrThrow(DEFAULT_PROJECT_ID)

        assertThat(localDbKey.projectId).isEqualTo(DEFAULT_PROJECT_ID)
    }

    @Test
    fun getLocalDbKeyOrThrow_shouldMigrateLegacyLocalKeyIfPresent() {
        val legacyKey = DEFAULT_REALM_KEY
        every { legacyLocalDbKeyProviderMock.getLocalDbKeyOrThrow(DEFAULT_PROJECT_ID) } returns
            LocalDbKey(DEFAULT_PROJECT_ID, legacyKey)

        secureLocalDbKeyProvider.getLocalDbKeyOrThrow(DEFAULT_PROJECT_ID)
        val localKey = secureLocalDbKeyProvider.getLocalDbKeyOrThrow(DEFAULT_PROJECT_ID)

        verify(exactly = 1) { legacyLocalDbKeyProviderMock.getLocalDbKeyOrThrow(DEFAULT_PROJECT_ID) }
        assertThat(localKey.value).isEqualTo(legacyKey)
        assertThat(realmKeyStored).isEqualTo(encodeToString(legacyKey, DEFAULT))
    }

    @Test
    fun getLocalDbKeyOrThrow_shouldThrowIfLocalKeyNotPresent() {
        getLocalDbKeyAndVerifyThatMissingKeyWasThrown()
    }

    @Test
    fun legacyLocalDbProviderThrows_secureLocalDbKeyProviderShouldThrowMissingLocalDatabaseKeyException() {
        every { legacyLocalDbKeyProviderMock.getLocalDbKeyOrThrow(any()) } throws IOException("some error")
        getLocalDbKeyAndVerifyThatMissingKeyWasThrown()

        verify { legacyLocalDbKeyProviderMock.getLocalDbKeyOrThrow(any()) }
    }

    private fun getLocalDbKeyAndVerifyThatMissingKeyWasThrown() {
        assertThrows<MissingLocalDatabaseKeyException> {
            secureLocalDbKeyProvider.getLocalDbKeyOrThrow(DEFAULT_PROJECT_ID)
        }
    }

    private fun mockStoredRealmKeyInSharedPrefs() =
        mockk<SharedPreferences>().also {
            every { it.getString(any(), any()) } returns encodeToString(DEFAULT_REALM_KEY, DEFAULT)
        }
}
