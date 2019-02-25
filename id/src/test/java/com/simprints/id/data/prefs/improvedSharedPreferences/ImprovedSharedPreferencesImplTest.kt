package com.simprints.id.data.prefs.improvedSharedPreferences

import android.content.SharedPreferences
import com.simprints.id.exceptions.unsafe.MismatchedTypeError
import com.simprints.id.exceptions.unsafe.NonPrimitiveTypeError
import com.simprints.testtools.common.syntax.assertThrows
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.verifyOnlyInteraction
import com.simprints.testtools.common.syntax.whenever
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import kotlin.reflect.KClass

class ImprovedSharedPreferencesImplTest {

    companion object {
        const val aKey = "aKey"

        const val storedInt: Int = -1
        const val storedLong: Long = -2
        const val storedFloat: Float = -3.0f
        const val storedString: String = "storedString"
        const val storedBoolean: Boolean = true

        const val aByte: Byte = 1
        const val aShort: Short = 2
        const val anInt: Int = 3
        const val aLong: Long = 4
        const val aFloat: Float = 5.0f
        const val aDouble: Double = 6.0
        const val aBoolean: Boolean = false
        const val aString: String = "aString"
        val aClass: KClass<*> = ImprovedSharedPreferencesImplTest::class
    }

    private val basePrefs: SharedPreferences =
            mockBasePrefsWithValues(storedInt, storedLong, storedFloat, storedString, storedBoolean)

    private val improvedPrefs: ImprovedSharedPreferences =
            ImprovedSharedPreferencesImpl(basePrefs)

    private fun mockBasePrefsWithValues(int: Int, long: Long, float: Float, string: String,
                                        boolean: Boolean): SharedPreferences {
        val prefs = mock<SharedPreferences>()
        whenever(prefs) { getInt(anyString(), anyInt()) } thenReturn int
        whenever(prefs) { getLong(anyString(), anyLong()) } thenReturn long
        whenever(prefs) { getFloat(anyString(), anyFloat()) } thenReturn float
        whenever(prefs) { getString(anyString(), anyString()) } thenReturn string
        whenever(prefs) { getBoolean(anyString(), anyBoolean()) } thenReturn boolean
        return prefs
    }

    @Test
    fun testGetPrimitiveGetsIntWhenDefaultIsByte() {
        improvedPrefs.getPrimitive(aKey, aByte)
        verifyOnlyInteraction(basePrefs) { getInt(aKey, aByte.toInt()) }
    }

    @Test
    fun testGetPrimitiveReturnsStoredIntAsByteWhenDefaultIsByte() {
        assertEquals(storedInt.toByte(), improvedPrefs.getPrimitive(aKey, aByte))
    }

    @Test
    fun testGetPrimitiveGetsIntWhenDefaultIsShort() {
        improvedPrefs.getPrimitive(aKey, aShort)
        verifyOnlyInteraction(basePrefs) { getInt(aKey, aShort.toInt()) }
    }

    @Test
    fun testGetPrimitiveReturnsStoredIntAsShortWhenDefaultIsShort() {
        assertEquals(storedInt.toShort(), improvedPrefs.getPrimitive(aKey, aShort))
    }

    @Test
    fun testGetPrimitiveGetsIntWhenDefaultIsInt() {
        improvedPrefs.getPrimitive(aKey, anInt)
        verifyOnlyInteraction(basePrefs) { getInt(aKey, anInt) }
    }

    @Test
    fun testGetPrimitiveReturnsStoredIntWhenDefaultIsInt() {
        assertEquals(storedInt, improvedPrefs.getPrimitive(aKey, anInt))
    }

    @Test
    fun testGetPrimitiveGetsLongWhenDefaultIsLong() {
        improvedPrefs.getPrimitive(aKey, aLong)
        verifyOnlyInteraction(basePrefs) { getLong(aKey, aLong) }
    }

    @Test
    fun testGetPrimitiveReturnsStoredLongWhenDefaultIsLong() {
        assertEquals(storedLong, improvedPrefs.getPrimitive(aKey, aLong))
    }

    @Test
    fun testGetPrimitiveGetsFloatWhenDefaultIsFloat() {
        improvedPrefs.getPrimitive(aKey, aFloat)
        verifyOnlyInteraction(basePrefs) { getFloat(aKey, aFloat) }
    }

    @Test
    fun testGetPrimitiveReturnsStoredFloatWhenDefaultIsFloat() {
        assertEquals(storedFloat, improvedPrefs.getPrimitive(aKey, aFloat))
    }

    @Test
    fun testGetPrimitiveGetsLongWhenDefaultIsDouble() {
        improvedPrefs.getPrimitive(aKey, aDouble)
        verifyOnlyInteraction(basePrefs) { getLong(aKey, aDouble.toRawBits()) }
    }

    @Test
    fun testGetPrimitiveReturnsStoredLongAsDoubleBitsWhenDefaultIsDouble() {
        assertEquals(Double.fromBits(storedLong), improvedPrefs.getPrimitive(aKey, aDouble), 0.0)
    }

    @Test
    fun testGetPrimitiveGetsBooleanWhenDefaultIsBoolean() {
        improvedPrefs.getPrimitive(aKey, aBoolean)
        verifyOnlyInteraction(basePrefs) { getBoolean(aKey, aBoolean) }
    }

    @Test
    fun testGetPrimitiveReturnsStoredBooleanWhenDefaultIsBoolean() {
        assertEquals(storedBoolean, improvedPrefs.getPrimitive(aKey, aBoolean))
    }

    @Test
    fun testGetPrimitiveGetsStringWhenDefaultIsString() {
        improvedPrefs.getPrimitive(aKey, aString)
        verifyOnlyInteraction(basePrefs) { getString(aKey, aString) }
    }

    @Test
    fun testGetPrimitiveReturnsStoredStringWhenDefaultIsString() {
        assertEquals(storedString, improvedPrefs.getPrimitive(aKey, aString))
    }

    @Test
    fun testGetPrimitiveWrapsExceptionsAsMismatchedTypeExceptions() {
        whenever { basePrefs.getInt(anyString(), anyInt()) } thenThrow ClassCastException()
        assertThrows<MismatchedTypeError> {
            improvedPrefs.getPrimitive(aKey, anInt)
        }
    }

    @Test
    fun testGetPrimitiveThrowsExceptionWhenValueIsUnsupportedType() {
        assertThrows<NonPrimitiveTypeException> {
            improvedPrefs.getPrimitive(aKey, aClass)
        }
    }
}
