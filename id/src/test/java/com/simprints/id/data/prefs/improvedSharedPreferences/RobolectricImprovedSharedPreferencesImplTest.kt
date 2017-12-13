package com.simprints.id.data.prefs.improvedSharedPreferences

import android.content.Context
import android.content.SharedPreferences
import com.simprints.id.exceptions.safe.MismatchedTypeException
import com.simprints.id.exceptions.unsafe.NonPrimitiveTypeError
import com.simprints.id.testUtils.assertThrows
import junit.framework.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.reflect.KClass
import org.mockito.Mockito.`when` as whenever

// TODO: Figure out why Roboletric is not happy when running tests with code coverage

/**
 * At first, I implemented getPrimitive() and putPrimitive() as extension functions of the SharedPreferences
 * interface.
 *
 * I had trouble mocking SharedPreferences to check that these functions would delegate their work
 * to the right SharedPreferences methods, so I used Robolectric.
 *
 * It allows doing "black box" testing, that is test ImprovedSharedPreferences without making any
 * assumption about the underlying implementation.
 */
@RunWith(RobolectricTestRunner::class)
class RobolectricImprovedSharedPreferencesImplTest {

    companion object {

        val sharedPrefName = "aSharedPrefName"

        val aKey = "aKey"
        val aByte: Byte = 1
        val aShort: Short = 2
        val anInt: Int  = 3
        val aLong: Long = 4
        val aFloat: Float = 5.0f
        val aDouble: Double = 6.0
        val aString: String = "aString"
        val aBoolean: Boolean = false
        val aClass: KClass<*> = RobolectricImprovedSharedPreferencesImplTest::class

        val storedByte: Byte = -1
        val storedShort: Short = -2
        val storedInt: Int  = -3
        val storedLong: Long = -4
        val storedFloat: Float = -5.0f
        val storedDouble: Double = -6.0
        val storedString: String = "storedString"
        val storedBoolean: Boolean = true

    }

    private val basePrefs: SharedPreferences = RuntimeEnvironment.application
            .getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

    private val improvedPrefs: ImprovedSharedPreferences = ImprovedSharedPreferencesImpl(basePrefs)


    @Test
    fun testGetPrimitiveReturnsBytePassedToPutPrimitive() {
        improvedPrefs.edit().putPrimitive(aKey, storedByte).apply()
        Assert.assertEquals(storedByte, improvedPrefs.getPrimitive(aKey, aByte))
    }

    @Test
    fun testGetPrimitiveReturnsShortPassedToPutPrimitive() {
        improvedPrefs.edit().putPrimitive(aKey, storedShort).apply()
        Assert.assertEquals(storedShort, improvedPrefs.getPrimitive(aKey, aShort))
    }

    @Test
    fun testGetPrimitiveReturnsIntPassedToPutPrimitive() {
        improvedPrefs.edit().putPrimitive(aKey, storedInt).apply()
        Assert.assertEquals(storedInt, improvedPrefs.getPrimitive(aKey, anInt))
    }

    @Test
    fun testGetPrimitiveReturnsLongPassedToPutPrimitive() {
        improvedPrefs.edit().putPrimitive(aKey, storedLong).apply()
        Assert.assertEquals(storedLong, improvedPrefs.getPrimitive(aKey, aLong))
    }

    @Test
    fun testGetPrimitiveReturnsFloatPassedToPutPrimitive() {
        improvedPrefs.edit().putPrimitive(aKey, storedFloat).apply()
        Assert.assertEquals(storedFloat, improvedPrefs.getPrimitive(aKey, aFloat))
    }

    @Test
    fun testGetPrimitiveReturnsDoublePassedToPutPrimitive() {
        improvedPrefs.edit().putPrimitive(aKey, storedDouble).apply()
        Assert.assertEquals(storedDouble, improvedPrefs.getPrimitive(aKey, aDouble))
    }

    @Test
    fun testGetPrimitiveReturnsStringPassedToPutPrimitive() {
        improvedPrefs.edit().putPrimitive(aKey, storedString).apply()
        Assert.assertEquals(storedString, improvedPrefs.getPrimitive(aKey, aString))
    }

    @Test
    fun testGetPrimitiveReturnsBooleanPassedToPutPrimitive() {
        improvedPrefs.edit().putPrimitive(aKey, storedBoolean).apply()
        Assert.assertEquals(storedBoolean, improvedPrefs.getPrimitive(aKey, aBoolean))
    }

    @Test
    fun testGetPrimitiveThrowsMismatchedTypeExceptionIfMismatchedTypes() {
        improvedPrefs.edit().putPrimitive(aKey, storedBoolean).apply()
        assertThrows<MismatchedTypeException> {
            improvedPrefs.getPrimitive(aKey, anInt)
        }
    }

    @Test
    fun testGetPrimitiveThrowsExceptionWhenValueIsUnsupportedType() {
        assertThrows<NonPrimitiveTypeError> {
            improvedPrefs.getPrimitive(aKey, aClass)
        }
    }


}