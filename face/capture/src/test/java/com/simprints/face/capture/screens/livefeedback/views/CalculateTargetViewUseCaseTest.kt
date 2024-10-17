package com.simprints.face.capture.screens.livefeedback.views

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class CalculateTargetViewUseCaseTest {

    @RelaxedMockK
    private lateinit var context: Context
    private lateinit var displayMetrics: DisplayMetrics

    private lateinit var calculateTargetViewSize: CalculateTargetViewSizeUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        displayMetrics = DisplayMetrics()

        every { context.resources.displayMetrics } returns displayMetrics

        calculateTargetViewSize = CalculateTargetViewSizeUseCase(context)
    }

    @Test
    fun `when smaller dimension is greater than 240dp, should return scaled target size`() {
        // Given
        displayMetrics.widthPixels = 800
        displayMetrics.heightPixels = 1200
        val expectedScaledTargetSize = 400f // 800 * 0.5f = 400px

        // When
        val result = calculateTargetViewSize()

        // Then
        assertThat(expectedScaledTargetSize).isEqualTo(result)
    }

    @Test
    fun `when smaller dimension is less than 240dp, should return base target size`() {
        // Given
        displayMetrics.widthPixels = 400
        displayMetrics.heightPixels = 300
        val baseTargetSizePx = 240f
        mockkStatic(TypedValue::class)
        every {
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, any(), any())
        } returns baseTargetSizePx

        // When
        val result = calculateTargetViewSize()

        // Then
        assertThat(baseTargetSizePx).isEqualTo(result)
    }
}
