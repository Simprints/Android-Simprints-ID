package com.simprints.fingerprint.tools.extensions

import android.content.Context
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowLooper

@RunWith(AndroidJUnit4::class)
class ActivityExtTest {

    @Before
    fun setUp() {
        mockkStatic(Looper::class)
    }

    @Test
    fun `test runOnUiThread if already in mainthread`() {
        // Given
        every { Looper.myLooper() } returns Looper.getMainLooper()
        // When
        var x =1
        val context = spyk<Context>()
        context.runOnUiThread{
            x =2
        }
        // Then
        assertThat(x).isEqualTo(2)
    }
    @Test
    fun `test runOnUiThread from another thread`() {
        // Given
        every { Looper.myLooper() } returns mockk(relaxed = true)

        // When
        var x =1
        val context = spyk<Context>()
        context.runOnUiThread{
            x =2
        }
        ShadowLooper.idleMainLooper()
        // Then
        assertThat(x).isEqualTo(2)
    }
}
