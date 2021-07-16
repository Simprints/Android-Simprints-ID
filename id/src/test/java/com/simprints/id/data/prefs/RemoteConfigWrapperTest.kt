package com.simprints.id.data.prefs

import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.prefs.RemoteConfigWrapper.Companion.PROJECT_SETTINGS_JSON_STRING_DEFAULT
import com.simprints.id.data.prefs.RemoteConfigWrapper.Companion.PROJECT_SETTINGS_JSON_STRING_KEY
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferencesImpl
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Needs to be Robolectric because JSONObject (in RemoteConfigWrapper) is inside Android SDK
 */
@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class RemoteConfigWrapperTest {

    private val sharedPreferences: SharedPreferences = mockk()
    private val improvedSharedPreferences = ImprovedSharedPreferencesImpl(sharedPreferences)
    private val remoteConfigWrapper = RemoteConfigWrapper(improvedSharedPreferences)

    @Test
    fun `get string from empty json should return null`() {
        val preferenceValue = remoteConfigWrapper.getString("aKey")

        assertThat(preferenceValue).isNull()
    }

    @Test
    fun `get string from json should return the value`() {
        every {
            sharedPreferences.getString(
                PROJECT_SETTINGS_JSON_STRING_KEY,
                PROJECT_SETTINGS_JSON_STRING_DEFAULT
            )
        } returns "{\"aKey\": \"USER\"}"

        assertThat(remoteConfigWrapper.projectSettingsJsonString).isEqualTo("{\"aKey\": \"USER\"}")

        val preferenceValue = remoteConfigWrapper.getString("aKey")

        verify {
            sharedPreferences.getString(
                PROJECT_SETTINGS_JSON_STRING_KEY,
                PROJECT_SETTINGS_JSON_STRING_DEFAULT
            )
        }
        assertThat(preferenceValue).isEqualTo("USER")
    }
}
