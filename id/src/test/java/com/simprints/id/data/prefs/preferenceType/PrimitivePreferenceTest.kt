package com.simprints.id.data.prefs.preferenceType

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.exceptions.unsafe.NonPrimitiveTypeError
import com.simprints.testframework.common.syntax.*
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.*

class PrimitivePreferenceTest {

    companion object {
        const val aKey = "aKey"
        const val aString = "0"
        const val storedString = "1"
        val aClass = PrimitivePreferenceTest::class.java
    }

    private val improvedEditor: ImprovedSharedPreferences.Editor = mockImprovedEditor()
    private val improvedPrefs: ImprovedSharedPreferences = mockImprovedPrefs(improvedEditor)
    private var stringPref by PrimitivePreference(improvedPrefs, aKey, aString)

    private fun mockImprovedEditor(): ImprovedSharedPreferences.Editor {
        val editor = com.simprints.testframework.common.syntax.mock<ImprovedSharedPreferences.Editor>()
        whenever(editor.putPrimitive(anyString(), anyNotNull())).thenReturn(editor)
        whenever(editor.putPrimitive(anyString(), anyNotNull())).thenReturn(editor)
        return editor
    }

    private fun mockImprovedPrefs(editorToReturn: ImprovedSharedPreferences.Editor): ImprovedSharedPreferences {
        val prefs = com.simprints.testframework.common.syntax.mock<ImprovedSharedPreferences>()
        whenever(prefs.edit()).thenReturn(editorToReturn)
        whenever(prefs.getPrimitive(aKey, aString)).thenReturn(storedString)
        return prefs
    }

    @Test
    fun testDeclarationDoesNotReadPreferences() {
        verifyZeroInteractions(improvedPrefs)
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testFirstGetValueReadsPreferencesWithGetPrimitive() {
        val firstGet = stringPref
        verifyOnlyInteraction(improvedPrefs) {
            getPrimitive(aKey, aString)
        }
    }

    @Test
    fun testFirstGetValueReturnsResultOfGetPrimitive() {
        Assert.assertEquals(storedString, stringPref)
    }

    @Suppress("UNUSED_VARIABLE", "UnnecessaryVariable")
    @Test
    fun testSecondGetValueDoesReadPreferences() {
        val firstGet = stringPref
        val secondGet = stringPref
        verify(improvedPrefs, times(2)).getPrimitive(aKey, aString)
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testSecondGetValueReturnsCachedValue() {
        val firstGet = stringPref
        val secondGet = stringPref
        Assert.assertEquals(storedString, secondGet)
    }

    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_VALUE")
    @Test
    fun testSetValuePutsPrimitiveAndAppliesChange() {
        stringPref = storedString
        verifyOnlyInteractions(improvedEditor,
            { putPrimitive(aKey, storedString) },
            { apply() })
    }

    @Suppress("UNUSED_VALUE", "UNUSED_VARIABLE")
    @Test
    fun testGetValueAfterSetValueDoesReadPreferences() {
        stringPref = storedString
        val firstGet = stringPref
        verify(improvedPrefs, times(1)).getPrimitive(aKey, aString)
    }

    @Suppress("UnnecessaryVariable")
    @Test
    fun testGetValueAfterSetValueReturnsValueSet() {
        stringPref = storedString
        Assert.assertEquals(storedString, stringPref)
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testDeclarationOfNonPrimitiveTypeThrowsNonPrimitiveException() {
        assertThrows<NonPrimitiveTypeError> {
            val classPref by PrimitivePreference(improvedPrefs, aKey, aClass)
        }
    }
}
