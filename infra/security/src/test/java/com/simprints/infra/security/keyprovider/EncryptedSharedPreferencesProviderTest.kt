package com.simprints.infra.security.keyprovider

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

internal class EncryptedSharedPreferencesProviderTest {
    @MockK
    lateinit var context: Context
    private lateinit var provider: EncryptedSharedPreferencesProvider
    private val filename = "filename"
    private val masterKeyAlias = "masterKeyAlias"

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        provider = EncryptedSharedPreferencesProvider(ctx = context)
    }

    @After
    fun cleanup() {
        unmockkAll()
    }

    @Test
    fun `when shared preferences are created should call EncryptedSharedPreferences_create`() {
        val spy = mockk<EncryptedSharedPreferences>()
        mockkStatic(EncryptedSharedPreferences::class)
        every { EncryptedSharedPreferences.create(any(), any(), any(), any(), any()) } returns spy
        provider.provideEncryptedSharedPreferences(
            filename = filename,
            masterKeyAlias = masterKeyAlias,
        )
        verify(exactly = 1) { EncryptedSharedPreferences.create(any(), any(), any(), any(), any()) }
    }
}
