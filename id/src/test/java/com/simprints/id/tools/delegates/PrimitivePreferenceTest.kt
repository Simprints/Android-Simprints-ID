package com.simprints.id.tools.delegates

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import java.security.InvalidParameterException
import org.mockito.Mockito.`when` as whenever

/**
 * @author: Etienne Thiery (etienne@simprints.com)
 */
class PrimitivePreferenceTest {

    companion object {
        val aKey = "aKey"
        val aString = "0"
        val anotherString = "1"
        val aClass = PrimitivePreferenceTest::class.java
    }

    private lateinit var prefs: ImprovedSharedPreferences
    private lateinit var editor: ImprovedSharedPreferences.Editor

    @Before
    fun setUp() {
        editor = Mockito.mock(ImprovedSharedPreferences.Editor::class.java)
        whenever(editor.putAny(anyString(), any())).thenReturn(editor)
        prefs = Mockito.mock(ImprovedSharedPreferences::class.java)
        whenever(prefs.getAny(aKey, aString)).thenReturn(anotherString)
        whenever(prefs.edit()).thenReturn(editor)
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testLazyInitialization() {
        val stringPref by PrimitivePreference(prefs, aKey, aString)
        verifyZeroInteractions(prefs)
        val firstAccess = stringPref
        verify(prefs).getAny(aKey, aString)
        verifyNoMoreInteractions(prefs)
    }

    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_VALUE")
    @Test
    fun testSetValue() {
        var stringPref by PrimitivePreference(prefs, aKey, aString)
        stringPref = anotherString
        verify(editor).putAny(aKey, anotherString)
        verify(editor).apply()
        verifyNoMoreInteractions(editor)
    }

    @Suppress("UnnecessaryVariable")
    @Test
    fun testGetValueFirstAccess() {
        val stringPref by PrimitivePreference(prefs, aKey, aString)
        Assert.assertEquals("getValue() should return whatever prefs.getAny() returns on first access",
                anotherString, stringPref)
    }

    @Suppress("UnnecessaryVariable")
    @Test
    fun testGetValueAfterSet() {
        var stringPref by PrimitivePreference(prefs, aKey, aString)
        stringPref = anotherString
        Assert.assertEquals("getValue() should return whatever value was set with setValue()",
                anotherString, stringPref)
    }


    @Suppress("UNUSED_VARIABLE", "UnnecessaryVariable")
    @Test
    fun testCachingOnGet() {
        val stringPref by PrimitivePreference(prefs, aKey, aString)
        val firstGet = stringPref
        verify(prefs, times(1)).getAny(aKey, aString)
        val secondGet = stringPref
        verifyNoMoreInteractions(prefs)
        Assert.assertEquals("getValue() should cache the value read in Shared Preferences",
                anotherString, secondGet)
    }

    @Suppress("UNUSED_VALUE", "UNUSED_VARIABLE")
    @Test
    fun testCachingOnSet() {
        var stringPref by PrimitivePreference(prefs, aKey, aString)
        stringPref = anotherString
        val get = stringPref
        verify(prefs, never()).getAny(aKey, aString)
    }

    @Suppress("UNUSED_VARIABLE")
    @Test(expected = InvalidParameterException::class)
    fun testInvalidTypeDetection() {
        val classPref by PrimitivePreference(prefs, aKey, aClass)
    }

    // TODO: figure out how to test thread safety

}