package com.simprints.id.data.prefs.improvedSharedPreferences

import android.content.SharedPreferences
import com.simprints.id.testUtils.assertThrows
import com.simprints.id.testUtils.mock
import com.simprints.id.testUtils.verifyOnlyInteraction
import com.simprints.id.testUtils.whenever
import com.simprints.id.tools.exceptions.MismatchedTypeException
import com.simprints.id.tools.exceptions.NonPrimitiveTypeException
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import kotlin.reflect.KClass


class ImprovedSharedPreferencesImplTest {

    companion object {
        val aKey = "aKey"

        val storedInt: Int = -1
        val storedLong: Long = -2
        val storedFloat: Float = -3.0f
        val storedString: String = "storedString"
        val storedBoolean: Boolean = true

        val aByte: Byte = 1
        val aShort: Short = 2
        val anInt: Int = 3
        val aLong: Long = 4
        val aFloat: Float = 5.0f
        val aDouble: Double = 6.0
        val aBoolean: Boolean = false
        val aString: String = "aString"
        val aClass: KClass<*> = ImprovedSharedPreferencesImplTest::class
    }

    private val basePrefs: SharedPreferences =
            mockBasePrefsWithValues(storedInt, storedLong, storedFloat, storedString, storedBoolean)

    private val improvedPrefs: ImprovedSharedPreferences =
            ImprovedSharedPreferencesImpl(basePrefs)

    private fun mockBasePrefsWithValues(int: Int, long: Long, float: Float, string: String,
                                        boolean: Boolean): SharedPreferences {
        val prefs = mock<SharedPreferences>()
        whenever(prefs.getInt(anyString(), anyInt())).thenReturn(int)
        whenever(prefs.getLong(anyString(), anyLong())).thenReturn(long)
        whenever(prefs.getFloat(anyString(), anyFloat())).thenReturn(float)
        whenever(prefs.getString(anyString(), anyString())).thenReturn(string)
        whenever(prefs.getBoolean(anyString(), anyBoolean())).thenReturn(boolean)
        return prefs
    }

    @Test
    fun testGetPrimitiveGetsIntWhenDefaultIsByte() {
        improvedPrefs.getPrimitive(aKey, aByte)
        verifyOnlyInteraction(basePrefs) { getInt(aKey, aByte.toInt())}
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
        assertEquals(Double.fromBits(storedLong), improvedPrefs.getPrimitive(aKey, aDouble))
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
        whenever(basePrefs.getInt(anyString(), anyInt())).then { throw ClassCastException() }
        assertThrows<MismatchedTypeException> {
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