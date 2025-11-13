package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Rect
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class GetBoundsRelativeToParentUseCaseTest {
    private lateinit var useCase: GetBoundsRelativeToParentUseCase

    companion object {
        private const val PARENT_WIDTH_LARGE = 1000
        private const val PARENT_HEIGHT_LARGE = 2000
        private const val PARENT_WIDTH_MEDIUM = 800
        private const val PARENT_HEIGHT_MEDIUM = 1200
        private const val PARENT_WIDTH_SMALL = 500
        private const val PARENT_HEIGHT_SMALL = 500

        private const val CHILD_WIDTH_MEDIUM = 300
        private const val CHILD_HEIGHT_MEDIUM = 400
        private const val CHILD_WIDTH_SMALL = 250
        private const val CHILD_HEIGHT_SMALL = 350
        private const val CHILD_WIDTH_LARGE = 200
        private const val CHILD_HEIGHT_LARGE = 200

        private const val LOCATION_X_ORIGIN = 100
        private const val LOCATION_Y_ORIGIN = 200
        private const val LOCATION_X_OFFSET = 150
        private const val LOCATION_Y_OFFSET = 250
        private const val LOCATION_X_DIFFERENT = 50
        private const val LOCATION_Y_DIFFERENT = 75
        private const val LOCATION_X_FAR = 200
        private const val LOCATION_Y_FAR = 300
        private const val LOCATION_X_OUTSIDE = 50
        private const val LOCATION_Y_OUTSIDE = 50
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = GetBoundsRelativeToParentUseCase()
    }

    @Test
    fun `calculates bounds when child is at parent origin`() {
        val (parent, child) = setupViews(
            parentLocation = intArrayOf(LOCATION_X_ORIGIN, LOCATION_Y_ORIGIN),
            childLocation = intArrayOf(LOCATION_X_ORIGIN, LOCATION_Y_ORIGIN),
            parentWidth = PARENT_WIDTH_LARGE,
            parentHeight = PARENT_HEIGHT_LARGE,
            childWidth = CHILD_WIDTH_MEDIUM,
            childHeight = CHILD_HEIGHT_MEDIUM,
        )

        val result = useCase(parent, child)

        val expectedRect = Rect(0, 0, CHILD_WIDTH_MEDIUM, CHILD_HEIGHT_MEDIUM)
        assertThat(result).isEqualTo(expectedRect)
    }

    @Test
    fun `calculates bounds when child is offset from parent`() {
        val (parent, child) = setupViews(
            parentLocation = intArrayOf(LOCATION_X_ORIGIN, LOCATION_Y_ORIGIN),
            childLocation = intArrayOf(LOCATION_X_OFFSET, LOCATION_Y_OFFSET),
            parentWidth = PARENT_WIDTH_LARGE,
            parentHeight = PARENT_HEIGHT_LARGE,
            childWidth = CHILD_WIDTH_MEDIUM,
            childHeight = CHILD_HEIGHT_MEDIUM,
        )

        val result = useCase(parent, child)

        val offsetX = LOCATION_X_OFFSET - LOCATION_X_ORIGIN
        val offsetY = LOCATION_Y_OFFSET - LOCATION_Y_ORIGIN
        val expectedRect = Rect(offsetX, offsetY, offsetX + CHILD_WIDTH_MEDIUM, offsetY + CHILD_HEIGHT_MEDIUM)
        assertThat(result).isEqualTo(expectedRect)
    }

    @Test
    fun `calculates bounds when parent and child have different screen positions`() {
        val (parent, child) = setupViews(
            parentLocation = intArrayOf(LOCATION_X_DIFFERENT, LOCATION_Y_DIFFERENT),
            childLocation = intArrayOf(LOCATION_X_FAR, LOCATION_Y_FAR),
            parentWidth = PARENT_WIDTH_MEDIUM,
            parentHeight = PARENT_HEIGHT_MEDIUM,
            childWidth = CHILD_WIDTH_SMALL,
            childHeight = CHILD_HEIGHT_SMALL,
        )

        val result = useCase(parent, child)

        val offsetX = LOCATION_X_FAR - LOCATION_X_DIFFERENT
        val offsetY = LOCATION_Y_FAR - LOCATION_Y_DIFFERENT
        val expectedRect = Rect(offsetX, offsetY, offsetX + CHILD_WIDTH_SMALL, offsetY + CHILD_HEIGHT_SMALL)
        assertThat(result).isEqualTo(expectedRect)
    }

    @Test
    fun `calculates bounds when child is partially outside parent bounds`() {
        val (parent, child) = setupViews(
            parentLocation = intArrayOf(LOCATION_X_ORIGIN, LOCATION_Y_ORIGIN),
            childLocation = intArrayOf(LOCATION_X_OUTSIDE, LOCATION_Y_OUTSIDE),
            parentWidth = PARENT_WIDTH_SMALL,
            parentHeight = PARENT_HEIGHT_SMALL,
            childWidth = CHILD_WIDTH_LARGE,
            childHeight = CHILD_HEIGHT_LARGE,
        )

        val result = useCase(parent, child)

        val offsetX = LOCATION_X_OUTSIDE - LOCATION_X_ORIGIN
        val offsetY = LOCATION_Y_OUTSIDE - LOCATION_Y_ORIGIN
        val expectedRect = Rect(offsetX, offsetY, offsetX + CHILD_WIDTH_LARGE, offsetY + CHILD_HEIGHT_LARGE)
        assertThat(result).isEqualTo(expectedRect)
    }

    private fun setupViews(
        parentLocation: IntArray,
        childLocation: IntArray,
        parentWidth: Int,
        parentHeight: Int,
        childWidth: Int,
        childHeight: Int,
    ): Pair<View, View> {
        val parent = mockk<View>()
        val child = mockk<View>()

        mockkStatic("android.view.View")
        every { parent.getLocationOnScreen(any()) } answers {
            val location = firstArg<IntArray>()
            location[0] = parentLocation[0]
            location[1] = parentLocation[1]
        }
        every { child.getLocationOnScreen(any()) } answers {
            val location = firstArg<IntArray>()
            location[0] = childLocation[0]
            location[1] = childLocation[1]
        }

        every { parent.width } returns parentWidth
        every { parent.height } returns parentHeight
        every { child.width } returns childWidth
        every { child.height } returns childHeight

        return Pair(parent, child)
    }
}
