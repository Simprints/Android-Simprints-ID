package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.externalcredential.screens.scanocr.model.LightingConditionsAssessment
import com.simprints.feature.externalcredential.screens.scanocr.model.LightingConditionsAssessmentConfig
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
internal class GetLightingConditionsAssessmentUseCaseTest {
    private lateinit var useCase: GetLightingConditionsAssessmentUseCase

    private val normalCardBitmap = createCardBitmap()
    private val dimCardBitmap = createCardBitmap(
        cardColor = "#3F3F3F".toColor(),
        cardTextColor = "#111111".toColor(),
    )
    private val brightCardBitmap = createCardBitmap(
        cardColor = "#FAFAFA".toColor(),
        cardTextColor = "#CCCCCC".toColor(),
    )
    private val glareCardBitmap = createCardBitmap(
        glareHotspotColor = "#FDFDFD".toColor(),
        glareNearAreaColor = "#CCFDFDFD".toColor(),
    )

    @Before
    fun setUp() {
        useCase = GetLightingConditionsAssessmentUseCase()
    }

    @Test
    fun `returns NORMAL when card is normally illuminated - for different image sizes`() {
        assertAssessment(expected = LightingConditionsAssessment.NORMAL, bitmap = normalCardBitmap)
        assertAssessment(expected = LightingConditionsAssessment.NORMAL, bitmap = normalCardBitmap.scaledByFactor(0.3))
        assertAssessment(expected = LightingConditionsAssessment.NORMAL, bitmap = normalCardBitmap.scaledByFactor(3.0))
    }

    @Test
    fun `returns TOO_DIM when card is dimly illuminated - for different image sizes`() {
        assertAssessment(expected = LightingConditionsAssessment.TOO_DIM, bitmap = dimCardBitmap)
        assertAssessment(expected = LightingConditionsAssessment.TOO_DIM, bitmap = dimCardBitmap.scaledByFactor(0.3))
        assertAssessment(expected = LightingConditionsAssessment.TOO_DIM, bitmap = dimCardBitmap.scaledByFactor(3.0))
    }

    @Test
    fun `returns TOO_BRIGHT when card is brightly illuminated - for different image sizes`() {
        assertAssessment(expected = LightingConditionsAssessment.TOO_BRIGHT, bitmap = brightCardBitmap)
        assertAssessment(expected = LightingConditionsAssessment.TOO_BRIGHT, bitmap = brightCardBitmap.scaledByFactor(0.3))
        assertAssessment(expected = LightingConditionsAssessment.TOO_BRIGHT, bitmap = brightCardBitmap.scaledByFactor(3.0))
    }

    @Test
    fun `returns TOO_BRIGHT when card is glare-illuminated - for different image sizes`() {
        assertAssessment(expected = LightingConditionsAssessment.TOO_BRIGHT, bitmap = glareCardBitmap)
        assertAssessment(expected = LightingConditionsAssessment.TOO_BRIGHT, bitmap = glareCardBitmap.scaledByFactor(0.3))
        assertAssessment(expected = LightingConditionsAssessment.TOO_BRIGHT, bitmap = glareCardBitmap.scaledByFactor(3.0))
    }

    @Test
    fun `returns NORMAL when card is not normally illuminated but isEnabled is false`() {
        assertAssessment(
            expected = LightingConditionsAssessment.NORMAL,
            bitmap = brightCardBitmap,
            config = getLightingConditionsAssessmentConfig(isEnabled = false),
        )
    }

    @Test
    fun `returns NORMAL when bitmap width is 0`() {
        val bitmap = mockk<Bitmap>(relaxed = true) {
            every { this@mockk.width } returns 0
            every { this@mockk.height } returns 1
        }

        val assessment = useCase(bitmap, getLightingConditionsAssessmentConfig())

        assertThat(assessment).isEqualTo(LightingConditionsAssessment.NORMAL)
    }

    @Test
    fun `returns NORMAL when bitmap height is 0`() {
        val bitmap = mockk<Bitmap>(relaxed = true) {
            every { this@mockk.width } returns 1
            every { this@mockk.height } returns 0
        }

        val assessment = useCase(bitmap, getLightingConditionsAssessmentConfig())

        assertThat(assessment).isEqualTo(LightingConditionsAssessment.NORMAL)
    }

    @Test
    fun `returns NORMAL when card is not normally illuminated but no padding allows a lot of contrasting background`() {
        assertAssessment(
            expected = LightingConditionsAssessment.NORMAL,
            bitmap = brightCardBitmap,
            config = getLightingConditionsAssessmentConfig(borderWidthPercent = 0),
        )
    }

    @Test
    fun `returns NORMAL when card is dimly illuminated but low brightness threshold is low`() {
        assertAssessment(
            expected = LightingConditionsAssessment.NORMAL,
            bitmap = dimCardBitmap,
            config = getLightingConditionsAssessmentConfig(lowMedianLuminanceThresholdPercent = 1),
        )
    }

    @Test
    fun `returns NORMAL when card is dimly illuminated but contrast threshold is low`() {
        assertAssessment(
            expected = LightingConditionsAssessment.NORMAL,
            bitmap = dimCardBitmap,
            config = getLightingConditionsAssessmentConfig(lowContrastThresholdPercent = 10),
        )
    }

    @Test
    fun `returns NORMAL when card is brightly illuminated but high brightness threshold is high`() {
        assertAssessment(
            expected = LightingConditionsAssessment.NORMAL,
            bitmap = brightCardBitmap,
            config = getLightingConditionsAssessmentConfig(highMedianLuminanceThresholdPercent = 99),
        )
    }

    @Test
    fun `returns NORMAL when card is brightly illuminated but contrast threshold is low`() {
        assertAssessment(
            expected = LightingConditionsAssessment.NORMAL,
            bitmap = brightCardBitmap,
            config = getLightingConditionsAssessmentConfig(lowContrastThresholdPercent = 10),
        )
    }

    @Test
    fun `returns NORMAL when card is glare illuminated but glare brightness threshold is high`() {
        assertAssessment(
            expected = LightingConditionsAssessment.NORMAL,
            bitmap = glareCardBitmap,
            config = getLightingConditionsAssessmentConfig(highGlareLuminanceThresholdPercent = 100),
        )
    }

    @Test
    fun `returns NORMAL when card is glare illuminated but near-glare contrast threshold is low`() {
        assertAssessment(
            expected = LightingConditionsAssessment.NORMAL,
            bitmap = glareCardBitmap,
            config = getLightingConditionsAssessmentConfig(lowContrastThresholdPercent = 10),
        )
    }

    @Test
    fun `returns NORMAL when card is glare illuminated but glare detection sensitivity grid size is low`() {
        assertAssessment(
            expected = LightingConditionsAssessment.NORMAL,
            bitmap = glareCardBitmap,
            config = getLightingConditionsAssessmentConfig(glareDetectionGridMinDimension = 2),
        )
    }

    @Test
    fun `does not recycle bitmap`() {
        useCase(normalCardBitmap, getLightingConditionsAssessmentConfig())
        val otherSizeCardBitmap = createCardBitmap(widthPx = 100, heightPx = 500)
        useCase(otherSizeCardBitmap, getLightingConditionsAssessmentConfig())

        assertThat(normalCardBitmap.isRecycled).isFalse()
        assertThat(otherSizeCardBitmap.isRecycled).isFalse()
    }

    private fun assertAssessment(
        expected: LightingConditionsAssessment,
        bitmap: Bitmap,
        config: LightingConditionsAssessmentConfig = getLightingConditionsAssessmentConfig(),
    ) {
        val actual = useCase(bitmap, config)
        assertThat(actual).isEqualTo(expected)
    }

    private fun getLightingConditionsAssessmentConfig(
        isEnabled: Boolean = true,
        borderWidthPercent: Int = 5,
        lowContrastThresholdPercent: Int = 30,
        lowMedianLuminanceThresholdPercent: Int = 25,
        highMedianLuminanceThresholdPercent: Int = 95,
        highGlareLuminanceThresholdPercent: Int = 99,
        glareDetectionGridMinDimension: Int = 6,
    ) = LightingConditionsAssessmentConfig(
        isEnabled = isEnabled,
        borderWidthPercent = borderWidthPercent,
        lowContrastThresholdPercent = lowContrastThresholdPercent,
        lowMedianLuminanceThresholdPercent = lowMedianLuminanceThresholdPercent,
        highMedianLuminanceThresholdPercent = highMedianLuminanceThresholdPercent,
        highGlareLuminanceThresholdPercent = highGlareLuminanceThresholdPercent,
        glareDetectionGridMinDimension = glareDetectionGridMinDimension,
    )

    /**
     * A test card image bitmap. By default the card has normal lighting conditions.
     * Bitmap helpers below allow rendering Android bitmaps in test runs.
     */
    private fun createCardBitmap(
        widthPx: Int = 300,
        heightPx: Int = 200,
        backgroundColor: Int = "#777777".toColor(),
        backgroundPaddingPx: Int = 12,
        cardColor: Int = "#DDDDDD".toColor(),
        cardTextColor: Int = "#444444".toColor(),
        glareCenterPxX: Int = 100,
        glareCenterPxY: Int = 100,
        glareHotspotColor: Int = Color.TRANSPARENT,
        glareHotspotDiameterPx: Int = 40,
        glareNearAreaColor: Int = Color.TRANSPARENT,
        glareNearAreaDiameterPx: Int = 70,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(widthPx * heightPx) { backgroundColor }

        val cardLeft = backgroundPaddingPx
        val cardTop = backgroundPaddingPx
        val cardRight = widthPx - backgroundPaddingPx
        val cardBottom = heightPx - backgroundPaddingPx
        fillRect(pixels, widthPx, cardLeft, cardTop, cardRight, cardBottom, cardColor)

        drawTextPlaceholders(pixels, widthPx, cardLeft, cardTop, cardRight, cardBottom, cardTextColor)

        blendCircle(pixels, widthPx, heightPx, glareCenterPxX, glareCenterPxY, glareNearAreaDiameterPx, glareNearAreaColor)
        blendCircle(pixels, widthPx, heightPx, glareCenterPxX, glareCenterPxY, glareHotspotDiameterPx, glareHotspotColor)

        bitmap.setPixels(pixels, 0, widthPx, 0, 0, widthPx, heightPx)
        return bitmap
    }

    private fun drawTextPlaceholders(
        pixels: IntArray,
        width: Int,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        color: Int,
    ) {
        val lineHeight = 40
        val wordHeight = 20
        val wordWidth = 40
        val wordSpacing = 80
        var y = top + wordHeight
        while (y + lineHeight <= bottom) {
            var x = left + wordWidth
            while (x + wordSpacing <= right) {
                fillRect(pixels, width, x, y, x + wordWidth, y + wordHeight, color)
                x += wordSpacing
            }
            y += lineHeight
        }
    }

    private fun fillRect(
        pixels: IntArray,
        width: Int,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        color: Int,
    ) {
        for (y in top until bottom) {
            val rowOffset = y * width
            for (x in left until right) {
                pixels[rowOffset + x] = color
            }
        }
    }

    private fun blendCircle(
        pixels: IntArray,
        width: Int,
        height: Int,
        centerX: Int,
        centerY: Int,
        diameterPx: Int,
        color: Int,
    ) {
        if (Color.alpha(color) == 0 || diameterPx <= 0) return

        val radius = diameterPx / 2.0
        val radiusSquared = radius * radius
        val left = max(0, floor(centerX - radius).toInt())
        val top = max(0, floor(centerY - radius).toInt())
        val right = minOf(width - 1, floor(centerX + radius).toInt())
        val bottom = minOf(height - 1, floor(centerY + radius).toInt())

        for (y in top..bottom) {
            val dy = y - centerY
            val rowOffset = y * width
            for (x in left..right) {
                val dx = x - centerX
                if (dx * dx + dy * dy <= radiusSquared) {
                    val index = rowOffset + x
                    pixels[index] = color.blendOver(pixels[index])
                }
            }
        }
    }

    private fun Int.blendOver(background: Int): Int {
        val alpha = Color.alpha(this)
        if (alpha == 255) return this

        val inverseAlpha = 255 - alpha
        return Color.argb(
            255,
            (Color.red(this) * alpha + Color.red(background) * inverseAlpha) / 255,
            (Color.green(this) * alpha + Color.green(background) * inverseAlpha) / 255,
            (Color.blue(this) * alpha + Color.blue(background) * inverseAlpha) / 255,
        )
    }

    private fun String.toColor(): Int = Color.parseColor(this)

    private fun Bitmap.scaledByFactor(scaleFactor: Double): Bitmap =
        Bitmap.createScaledBitmap(this, (width * scaleFactor).roundToInt(), (height * scaleFactor).roundToInt(), true)

}
