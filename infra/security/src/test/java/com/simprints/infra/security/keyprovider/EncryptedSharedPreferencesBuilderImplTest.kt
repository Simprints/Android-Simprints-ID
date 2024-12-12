package com.simprints.infra.security.keyprovider

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.os.Build
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

internal class EncryptedSharedPreferencesBuilderImplTest {
    @MockK
    lateinit var context: Context

    @MockK
    lateinit var applicationInfo: ApplicationInfo

    @MockK
    lateinit var sharedPreferences: SharedPreferences

    @MockK
    lateinit var masterKeyProvider: MasterKeyProvider

    @MockK
    lateinit var preferencesProvider: EncryptedSharedPreferencesProvider

    private lateinit var builder: EncryptedSharedPreferencesBuilder
    private val dataDirectory = "dataDir"
    private val filename = "filename"
    private val masterKey = "masterKey"
    private val buildSdk = 30

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        builder = EncryptedSharedPreferencesBuilderImpl(
            ctx = context,
            masterKeyProvider = masterKeyProvider,
            preferencesProvider = preferencesProvider,
            buildSdk = buildSdk,
        )
        applicationInfo.dataDir = dataDirectory
        every { context.applicationInfo } returns applicationInfo
        every { context.applicationInfo } returns applicationInfo
        every {
            preferencesProvider.provideEncryptedSharedPreferences(any(), any())
        } returns sharedPreferences
        every { masterKeyProvider.provideMasterKey() } returns masterKey
    }

    @Test
    fun `should build encrypted shared preferences using preference provider and master key provider`() {
        val result = builder.buildEncryptedSharedPreferences(filename)
        verify(exactly = 1) { masterKeyProvider.provideMasterKey() }
        verify(exactly = 1) {
            preferencesProvider.provideEncryptedSharedPreferences(
                filename,
                masterKey,
            )
        }
        assertThat(result).isEqualTo(sharedPreferences)
    }

    @Test
    fun `should delete encrypted shared preferences in case of exception`() {
        every {
            preferencesProvider.provideEncryptedSharedPreferences(any(), any())
        } throws Exception() andThen sharedPreferences
        builder.buildEncryptedSharedPreferences(filename)
        verify(exactly = 1) { context.deleteSharedPreferences(filename) }
    }

    @Test
    fun `should create encrypted shared preferences in case of exception`() {
        every {
            preferencesProvider.provideEncryptedSharedPreferences(any(), any())
        } throws Exception() andThen sharedPreferences
        val result = builder.buildEncryptedSharedPreferences(filename)
        verify(exactly = 2) { masterKeyProvider.provideMasterKey() }
        verify(exactly = 2) {
            preferencesProvider.provideEncryptedSharedPreferences(
                filename,
                masterKey,
            )
        }
        assertThat(result).isEqualTo(sharedPreferences)
    }

    @Test
    fun `should delete encrypted shared preferences as file when version is M and lower`() {
        builder = EncryptedSharedPreferencesBuilderImpl(
            ctx = context,
            masterKeyProvider = masterKeyProvider,
            preferencesProvider = preferencesProvider,
            buildSdk = Build.VERSION_CODES.M,
        )
        every {
            preferencesProvider.provideEncryptedSharedPreferences(any(), any())
        } throws Exception() andThen sharedPreferences
        builder.buildEncryptedSharedPreferences(filename)
        verify(exactly = 0) { context.deleteSharedPreferences(filename) }
        verify(exactly = 1) { context.applicationInfo }
    }
}
