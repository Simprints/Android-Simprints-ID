package com.simprints.face.capture.usecases

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.simprints.infra.config.store.models.experimental
import com.simprints.infra.config.sync.ConfigManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IsUsingAutoCaptureUseCaseTest {

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var isUsingAutoCapture: IsUsingAutoCaptureUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mockkStatic(PreferenceManager::class)
        every { PreferenceManager.getDefaultSharedPreferences(context) } returns sharedPreferences

        isUsingAutoCapture = IsUsingAutoCaptureUseCase(configManager, context)
    }

    private fun setupParams(featureEnabled: Boolean, preferenceEnabled: Boolean) {
        coEvery { configManager.getProjectConfiguration().experimental().faceAutoCaptureEnabled } returns featureEnabled
        every { sharedPreferences.getBoolean("preference_enable_face_auto_capture", true) } returns preferenceEnabled
    }

    @Test
    fun `should use auto-capture when feature flag is enabled and preference is true`() = runTest {
        // Given
        setupParams(featureEnabled = true, preferenceEnabled = true)

        // When
        val result = isUsingAutoCapture()

        // Then
        assertTrue(result)
    }

    @Test
    fun `should not use auto-capture when feature flag is enabled but preference is false`() = runTest {
        // Given
        setupParams(featureEnabled = true, preferenceEnabled = false)

        // When
        val result = isUsingAutoCapture()

        // Then
        assertFalse(result)
    }

    @Test
    fun `should not use auto-capture when feature flag is disabled but preference is true`() = runTest {
        // Given
        setupParams(featureEnabled = false, preferenceEnabled = true)

        // When
        val result = isUsingAutoCapture()

        // Then
        assertFalse(result)
    }

    @Test
    fun `should not use auto-capture when both feature flag and preference are false`() = runTest {
        // Given
        setupParams(featureEnabled = false, preferenceEnabled = false)

        // When
        val result = isUsingAutoCapture()

        // Then
        assertFalse(result)
    }
}
