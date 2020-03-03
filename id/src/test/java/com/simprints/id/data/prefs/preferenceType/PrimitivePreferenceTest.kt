package com.simprints.id.data.prefs.preferenceType

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.exceptions.unexpected.NonPrimitiveTypeException
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import org.junit.Assert
import org.junit.Test

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
        val editor = mockk<ImprovedSharedPreferences.Editor>(relaxed = true)
        every { editor.putPrimitive(any(), any<String>()) } returns editor
        every { editor.putPrimitive(any(), any<String>()) } returns editor
        return editor
    }

    private fun mockImprovedPrefs(editorToReturn: ImprovedSharedPreferences.Editor): ImprovedSharedPreferences {
        val prefs = mockk<ImprovedSharedPreferences>(relaxed = true)
        every { prefs.edit() } returns editorToReturn
        every { prefs.getPrimitive(aKey, aString) } returns storedString
        return prefs
    }

    @Test
    fun testDeclarationDoesNotReadPreferences() {
        verify { improvedPrefs wasNot Called }
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testFirstGetValueReadsPreferencesWithGetPrimitive() {
        val firstGet = stringPref
        verifyAll { improvedPrefs.getPrimitive(aKey, aString) }
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
        verify(exactly = 2) { improvedPrefs.getPrimitive(aKey, aString) }
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
        verifyAll {
            improvedEditor.putPrimitive(aKey, storedString)
            improvedEditor.apply()
        }
    }

    @Suppress("UNUSED_VALUE", "UNUSED_VARIABLE")
    @Test
    fun testGetValueAfterSetValueDoesReadPreferences() {
        stringPref = storedString
        val firstGet = stringPref
        verify(exactly = 1) { improvedPrefs.getPrimitive(aKey, aString) }
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
        assertThrows<NonPrimitiveTypeException> {
            val classPref by PrimitivePreference(improvedPrefs, aKey, aClass)
        }
    }
}
