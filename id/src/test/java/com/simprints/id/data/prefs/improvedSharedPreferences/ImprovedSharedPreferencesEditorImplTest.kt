package com.simprints.id.data.prefs.improvedSharedPreferences

import android.content.SharedPreferences
import com.simprints.id.exceptions.unexpected.NonPrimitiveTypeException
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
import org.junit.Test

class ImprovedSharedPreferencesEditorImplTest {

    companion object {
        const val aKey = "aKey"
        const val aByte: Byte = 0
        const val aShort: Short = 1
        const val anInt: Int = 2
        const val aLong: Long = 3
        const val aFloat: Float = 4.0f
        const val aDouble: Double = 5.0
        const val aString = "aString"
        const val aBoolean = false
        val aClass = ImprovedSharedPreferencesImplTest::class
    }

    private val baseEditor: SharedPreferences.Editor = mockBaseEditor()

    private val improvedEditor: ImprovedSharedPreferences.Editor =
        ImprovedSharedPreferencesEditorImpl(baseEditor)

    private fun mockBaseEditor(): SharedPreferences.Editor {
        val editor = mockk<SharedPreferences.Editor>()
        every { editor.putString(any(), any()) } returns editor
        every { editor.putLong(any(), any()) } returns editor
        every { editor.putLong(any(), any()) } returns editor
        every { editor.putInt(any(), any()) } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.putFloat(any(), any()) } returns editor
        return editor
    }

    @Test
    fun testPutPrimitivePutsIntWhenValueIsByte() {
        improvedEditor.putPrimitive(aKey, aByte)
        verifyAll { baseEditor.putInt(aKey, aByte.toInt()) }
    }

    @Test
    fun testPutPrimitivePutsIntWhenValueIsShort() {
        improvedEditor.putPrimitive(aKey, aShort)
        verifyAll { baseEditor.putInt(aKey, aShort.toInt()) }
    }

    @Test
    fun testPutPrimitivePutsIntWhenValueIsInt() {
        improvedEditor.putPrimitive(aKey, anInt)
        verifyAll { baseEditor.putInt(aKey, anInt) }
    }

    @Test
    fun testPutPrimitivePutsLongWhenValueIsLong() {
        improvedEditor.putPrimitive(aKey, aLong)
        verifyAll { baseEditor.putLong(aKey, aLong) }
    }

    @Test
    fun testPutPrimitivePutsFloatWhenValueIsFloat() {
        improvedEditor.putPrimitive(aKey, aFloat)
        verifyAll { baseEditor.putFloat(aKey, aFloat) }
    }

    @Test
    fun testPutPrimitivePutsLongBytesWhenValueIsDouble() {
        improvedEditor.putPrimitive(aKey, aDouble)
        verifyAll { baseEditor.putLong(aKey, aDouble.toRawBits()) }
    }

    @Test
    fun testPutPrimitivePutsStringWhenValueIsString() {
        improvedEditor.putPrimitive(aKey, aString)
        verifyAll { baseEditor.putString(aKey, aString) }
    }

    @Test
    fun testPutPrimitivePutsBooleanWhenValueIsBoolean() {
        improvedEditor.putPrimitive(aKey, aBoolean)
        verifyAll { baseEditor.putBoolean(aKey, aBoolean) }
    }

    @Test
    fun testPutPrimitiveThrowsExceptionWhenValueIsUnsupportedType() {
        shouldThrow<NonPrimitiveTypeException> {
            improvedEditor.putPrimitive(aKey, aClass)
        }
    }
}
