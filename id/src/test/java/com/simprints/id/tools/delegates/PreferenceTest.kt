package com.simprints.id.tools.delegates

import com.simprints.id.tools.delegations.sharedPreferences.ExtSharedPreferences
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.Mockito.`when` as whenever

class PreferenceTest {

    companion object {
        val aKey = "aKey"
        val aString = "0"
        val anotherString = "1"

    }

    private lateinit var prefs: ExtSharedPreferences
    private lateinit var editor: ExtSharedPreferences.Editor

    @Before
    fun setUp() {
        prefs = Mockito.mock(ExtSharedPreferences::class.java)
        editor = Mockito.mock(ExtSharedPreferences.Editor::class.java)
        whenever(prefs.edit()).thenReturn(editor)
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun lazyInitialization() {
        val stringPref by Preference(prefs, aKey, aString)
        verifyZeroInteractions(prefs)
    }


    @Suppress("UnnecessaryVariable")
    @Test
    fun getValueOnFirstGet() {
        whenever(prefs.getAny(aKey, aString)).thenReturn(anotherString)
        val stringPref by Preference(prefs, aKey, aString)
        val firstAccess = stringPref
        verify(prefs).getAny(aKey, aString)
        verifyNoMoreInteractions(prefs)
        Assert.assertEquals("getValue() should call prefs.getAny() on first access",
                anotherString, firstAccess)
    }

    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_VALUE")
    @Test
    fun putValueOnSet() {
        whenever(editor.putAny(aKey, anotherString)).thenReturn(editor)
        var stringPref by Preference(prefs, aKey, aString)
        stringPref = anotherString
        verify(editor).putAny(aKey, anotherString)
        verify(editor).apply()
        verifyNoMoreInteractions(editor)
    }

    @Test
    fun consistentSetAndGet() {
        whenever(editor.putAny(aKey, anotherString)).thenReturn(editor)
        var stringPref by Preference(prefs, aKey, aString)
        stringPref = anotherString
        Assert.assertEquals("getValue() should return whatever value was set with setValue()",
                anotherString, stringPref)
    }

    @Suppress("UNUSED_VARIABLE", "UnnecessaryVariable")
    @Test
    fun cachingOnFirstGet() {
        whenever(prefs.getAny(aKey, aString)).thenReturn(anotherString)
        val stringPref by Preference(prefs, aKey, aString)
        val firstGet = stringPref
        verify(prefs, times(1)).getAny(aKey, aString)
        val secondGet = stringPref
        verifyNoMoreInteractions(prefs)
        Assert.assertEquals("The value of a property delegated to Preference should be cached",
                anotherString, secondGet)
    }

    @Suppress("UNUSED_VALUE", "UNUSED_VARIABLE")
    @Test
    fun cachingOnSet() {
        whenever(editor.putAny(aKey, anotherString)).thenReturn(editor)
        var stringPref by Preference(prefs, aKey, aString)
        stringPref = anotherString
        val get = stringPref
        verify(prefs, never()).getAny(aKey, aString)
    }

    // TODO: figure out how to test thread safety

}