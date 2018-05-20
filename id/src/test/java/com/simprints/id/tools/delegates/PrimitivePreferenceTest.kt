package com.simprints.id.tools.delegates

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.exceptions.unsafe.NonPrimitiveTypeError
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.*
import shared.assertThrows
import shared.verifyOnlyInteraction
import shared.verifyOnlyInteractions
import shared.whenever

class PrimitivePreferenceTest {

    companion object {
        val aKey = "aKey"
        val aString = "0"
        val storedString = "1"
        val aClass = PrimitivePreferenceTest::class.java
    }

    private val improvedEditor: ImprovedSharedPreferences.Editor = mockImprovedEditor()
    private val improvedPrefs: ImprovedSharedPreferences = mockImprovedPrefs(improvedEditor)
    private var stringPref by PrimitivePreference(improvedPrefs, aKey, aString)

    private fun mockImprovedEditor(): ImprovedSharedPreferences.Editor {
        val editor = shared.mock<ImprovedSharedPreferences.Editor>()
        whenever(editor.putPrimitive(anyString(), any())).thenReturn(editor)
        whenever(editor.putPrimitive(anyString(), any())).thenReturn(editor)
        return editor
    }

    private fun mockImprovedPrefs(editorToReturn: ImprovedSharedPreferences.Editor): ImprovedSharedPreferences {
        val prefs = shared.mock<ImprovedSharedPreferences>()
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
    fun testSecondGetValueDoesNotReadPreferences() {
        val firstGet = stringPref
        val secondGet = stringPref
        verifyOnlyInteraction(improvedPrefs) {
            getPrimitive(aKey, aString)
        }
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
    fun testGetValueAfterSetValueDoesNotReadPreferences() {
        stringPref = storedString
        val firstGet = stringPref
        verify(improvedPrefs, never()).getPrimitive(aKey, aString)
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

    // TODO: figure out how to test thread safety
}
