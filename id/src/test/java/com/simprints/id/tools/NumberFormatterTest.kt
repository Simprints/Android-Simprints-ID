package com.simprints.id.tools

import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.testUtils.roboletric.TestApplication
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class NumberFormatterTest {

    private val hindiLocale = Locale("hi")

    @Test
    fun ensureCommaSeparatorWorksWithEnglishCorrectly() {
        val rawNumber = 1234567890
        val targetEnglishNumber = "1,234,567,890"

        Assert.assertEquals(targetEnglishNumber,
            NumberFormatter(Locale.ENGLISH).getFormattedIntegerString(rawNumber))
    }

    @Test
    fun ensureCommaSeparatorWorksWithGermanCorrectly() {
        val rawNumber = 1234567890
        val targetGermanNumber = "1.234.567.890"

        Assert.assertEquals(targetGermanNumber,
            NumberFormatter(Locale.GERMAN).getFormattedIntegerString(rawNumber))
    }

    @Test
    fun ensureCommaSeparatorWorksWithHindiCorrectly() {
        val rawNumber = 1234567890
        val targetHindiNumber = "1,23,45,67,890"

        Assert.assertEquals(targetHindiNumber,
            NumberFormatter(hindiLocale).getFormattedIntegerString(rawNumber))
    }

    @Config(sdk = [19])
    @Test
    fun ensureCommaSeparatorWorksWithEnglishOnOldSdk() {
        val rawNumber = 1234567890
        val targetEnglishNumber = "1,234,567,890"

        Assert.assertEquals(targetEnglishNumber,
            NumberFormatter(Locale.ENGLISH).getFormattedIntegerString(rawNumber))
    }

    @Config(sdk = [19])
    @Test
    fun ensureCommaSeparatorDefaultsToEnglishWhenSetToHindiOnOldSdk() {
        val rawNumber = 1234567890
        val targetEnglishNumber = "1,234,567,890"

        Assert.assertEquals(targetEnglishNumber,
            NumberFormatter(hindiLocale).getFormattedIntegerString(rawNumber))
    }
}
