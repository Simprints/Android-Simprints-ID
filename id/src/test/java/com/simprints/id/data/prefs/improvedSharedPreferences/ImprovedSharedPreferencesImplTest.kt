package com.simprints.id.data.prefs.improvedSharedPreferences

import android.content.SharedPreferences
import com.simprints.id.exceptions.unexpected.MismatchedTypeException
import com.simprints.id.exceptions.unexpected.NonPrimitiveTypeException
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
import org.junit.Assert.assertEquals
import org.junit.Test
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
        val prefs = mockk<SharedPreferences>()
        every { prefs.getInt(any(), any()) } returns int
        every { prefs.getLong(any(), any()) } returns long
        every { prefs.getFloat(any(), any()) } returns float
        every { prefs.getString(any(), any()) } returns string
        every { prefs.getBoolean(any(), any()) } returns boolean
        return prefs
    }

    @Test
    fun testGetPrimitiveGetsIntWhenDefaultIsByte() {
        improvedPrefs.getPrimitive(aKey, aByte)
        verifyAll { basePrefs.getInt(aKey, aByte.toInt()) }
    }

    @Test
    fun testGetPrimitiveReturnsStoredIntAsByteWhenDefaultIsByte() {
        assertEquals(storedInt.toByte(), improvedPrefs.getPrimitive(aKey, aByte))
    }

    @Test
    fun testGetPrimitiveGetsIntWhenDefaultIsShort() {
        improvedPrefs.getPrimitive(aKey, aShort)
        verifyAll { basePrefs.getInt(aKey, aShort.toInt()) }
    }

    @Test
    fun testGetPrimitiveReturnsStoredIntAsShortWhenDefaultIsShort() {
        assertEquals(storedInt.toShort(), improvedPrefs.getPrimitive(aKey, aShort))
    }

    @Test
    fun testGetPrimitiveGetsIntWhenDefaultIsInt() {
        improvedPrefs.getPrimitive(aKey, anInt)
        verifyAll { basePrefs.getInt(aKey, anInt) }
    }

    @Test
    fun testGetPrimitiveReturnsStoredIntWhenDefaultIsInt() {
        assertEquals(storedInt, improvedPrefs.getPrimitive(aKey, anInt))
    }

    @Test
    fun testGetPrimitiveGetsLongWhenDefaultIsLong() {
        improvedPrefs.getPrimitive(aKey, aLong)
        verifyAll { basePrefs.getLong(aKey, aLong) }
    }

    @Test
    fun testGetPrimitiveReturnsStoredLongWhenDefaultIsLong() {
        assertEquals(storedLong, improvedPrefs.getPrimitive(aKey, aLong))
    }

    @Test
    fun testGetPrimitiveGetsFloatWhenDefaultIsFloat() {
        improvedPrefs.getPrimitive(aKey, aFloat)
        verifyAll { basePrefs.getFloat(aKey, aFloat) }
    }

    @Test
    fun testGetPrimitiveReturnsStoredFloatWhenDefaultIsFloat() {
        assertEquals(storedFloat, improvedPrefs.getPrimitive(aKey, aFloat))
    }

    @Test
    fun testGetPrimitiveGetsLongWhenDefaultIsDouble() {
        improvedPrefs.getPrimitive(aKey, aDouble)
        verifyAll { basePrefs.getLong(aKey, aDouble.toRawBits()) }
    }

    @Test
    fun testGetPrimitiveReturnsStoredLongAsDoubleBitsWhenDefaultIsDouble() {
        assertEquals(Double.fromBits(storedLong), improvedPrefs.getPrimitive(aKey, aDouble), 0.0)
    }

    @Test
    fun testGetPrimitiveGetsBooleanWhenDefaultIsBoolean() {
        improvedPrefs.getPrimitive(aKey, aBoolean)
        verifyAll { basePrefs.getBoolean(aKey, aBoolean) }
    }

    @Test
    fun testGetPrimitiveReturnsStoredBooleanWhenDefaultIsBoolean() {
        assertEquals(storedBoolean, improvedPrefs.getPrimitive(aKey, aBoolean))
    }

    @Test
    fun testGetPrimitiveGetsStringWhenDefaultIsString() {
        improvedPrefs.getPrimitive(aKey, aString)
        verifyAll { basePrefs.getString(aKey, aString) }
    }

    @Test
    fun testGetPrimitiveReturnsStoredStringWhenDefaultIsString() {
        assertEquals(storedString, improvedPrefs.getPrimitive(aKey, aString))
    }

    @Test
    fun testGetPrimitiveWrapsExceptionsAsMismatchedTypeExceptions() {
        every { basePrefs.getInt(any(), any()) } throws ClassCastException()
        shouldThrow<MismatchedTypeException> {
            improvedPrefs.getPrimitive(aKey, anInt)
        }
    }

    @Test
    fun testGetPrimitiveThrowsExceptionWhenValueIsUnsupportedType() {
        shouldThrow<NonPrimitiveTypeException> {
            improvedPrefs.getPrimitive(aKey, aClass)
        }
    }
}
