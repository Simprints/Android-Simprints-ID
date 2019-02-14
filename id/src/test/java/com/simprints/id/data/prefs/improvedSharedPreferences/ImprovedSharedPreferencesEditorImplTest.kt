package com.simprints.id.data.prefs.improvedSharedPreferences

import android.content.SharedPreferences
import com.simprints.id.shared.assertThrows
import com.simprints.id.shared.mock
import com.simprints.id.shared.verifyOnlyInteraction
import com.simprints.id.shared.whenever
import com.simprints.id.exceptions.unexpected.NonPrimitiveTypeError
import org.junit.Test
import org.mockito.ArgumentMatchers.*

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
        val editor = mock<SharedPreferences.Editor>()
        whenever(editor.putString(anyString(), anyString()))
                .thenReturn(editor)
        whenever(editor.putLong(anyString(), anyLong()))
                .thenReturn(editor)
        whenever(editor.putLong(anyString(), anyLong()))
                .thenReturn(editor)
        whenever(editor.putInt(anyString(), anyInt()))
                .thenReturn(editor)
        whenever(editor.putBoolean(anyString(), anyBoolean()))
                .thenReturn(editor)
        whenever(editor.putFloat(anyString(), anyFloat()))
                .thenReturn(editor)
        return editor
    }

    @Test
    fun testPutPrimitivePutsIntWhenValueIsByte() {
        improvedEditor.putPrimitive(aKey, aByte)
        verifyOnlyInteraction(baseEditor) { putInt(aKey, aByte.toInt()) }
    }

    @Test
    fun testPutPrimitivePutsIntWhenValueIsShort() {
        improvedEditor.putPrimitive(aKey, aShort)
        verifyOnlyInteraction(baseEditor) { putInt(aKey, aShort.toInt()) }
    }

    @Test
    fun testPutPrimitivePutsIntWhenValueIsInt() {
        improvedEditor.putPrimitive(aKey, anInt)
        verifyOnlyInteraction(baseEditor) { putInt(aKey, anInt) }
    }

    @Test
    fun testPutPrimitivePutsLongWhenValueIsLong() {
        improvedEditor.putPrimitive(aKey, aLong)
        verifyOnlyInteraction(baseEditor) { putLong(aKey, aLong) }
    }

    @Test
    fun testPutPrimitivePutsFloatWhenValueIsFloat() {
        improvedEditor.putPrimitive(aKey, aFloat)
        verifyOnlyInteraction(baseEditor) { putFloat(aKey, aFloat) }
    }

    @Test
    fun testPutPrimitivePutsLongBytesWhenValueIsDouble() {
        improvedEditor.putPrimitive(aKey, aDouble)
        verifyOnlyInteraction(baseEditor) { putLong(aKey, aDouble.toRawBits()) }
    }

    @Test
    fun testPutPrimitivePutsStringWhenValueIsString() {
        improvedEditor.putPrimitive(aKey, aString)
        verifyOnlyInteraction(baseEditor) { putString(aKey, aString) }
    }

    @Test
    fun testPutPrimitivePutsBooleanWhenValueIsBoolean() {
        improvedEditor.putPrimitive(aKey, aBoolean)
        verifyOnlyInteraction(baseEditor) { putBoolean(aKey, aBoolean) }
    }

    @Test
    fun testPutPrimitiveThrowsExceptionWhenValueIsUnsupportedType() {
        assertThrows<NonPrimitiveTypeError> {
            improvedEditor.putPrimitive(aKey, aClass)
        }
    }
}
