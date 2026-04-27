package com.simprints.core.tools.extensions

import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.*
import com.simprints.core.tools.extentions.area
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RectFExtTest {
    @Test
    fun `area should return correct value`() {
        val rect = RectF(0f, 0f, 2f, 3f)
        Truth.assertThat(rect.area()).isEqualTo(6f)
    }

    @Test
    fun `area should return positive value for negative dimensions`() {
        val rect = RectF(0f, 0f, -2f, -3f)
        Truth.assertThat(rect.area()).isEqualTo(6f)
    }
}
