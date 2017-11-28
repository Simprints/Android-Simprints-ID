package com.simprints.id.tools.delegations.sharedPreferences

import android.content.SharedPreferences
import com.simprints.id.model.Callout
import com.simprints.libdata.tools.Constants
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.Mockito.`when` as whenever

class ExtSharedPreferencesImplTest {

    companion object {

        val aKey = "key"
        val aString = "0"
        val anotherString = "1"
        val aLong = 0L
        val anotherLong = 1L
        val anInt = 0
        val anotherInt = 1
        val aBoolean = false
        val anotherBoolean = true
        val aFloat = 0f
        val anotherFloat = 1f
        val aGroup = Constants.GROUP.GLOBAL
        val anotherGroup = Constants.GROUP.USER
        val aCallout = Callout.NULL
        val anotherCallout = Callout.IDENTIFY
        val aClass = ExtSharedPreferencesImplTest::class
    }

    private lateinit var basePrefs: SharedPreferences
    private lateinit var prefs: ExtSharedPreferences

    private lateinit var baseEditor: SharedPreferences.Editor
    private lateinit var editor: ExtSharedPreferences.Editor

    @Before
    fun setUp() {
        baseEditor = mock(SharedPreferences.Editor::class.java)
        basePrefs = mock(SharedPreferences::class.java)
        prefs = ExtSharedPreferencesImpl(basePrefs)
        editor = ExtEditorImpl(baseEditor)
    }


    @Test
    fun getEnum() {
        // Note: No assumption should be made about the implementation of getEnum()
        // (are enum values stored as their ordinal? as their name?).
        // I could not find a way to test it regardless of the implementation without
        // using Robolectric (see RobolectricExtSharedPreferencesImpl)
        whenever(basePrefs.getString(aKey, aGroup.name)).thenReturn(anotherGroup.name)
        Assert.assertEquals("getEnum() should getString() an enum name and convert it to an enum value",
                anotherGroup, prefs.getEnum(aKey, aGroup))
    }

    @Test
    fun getAnyString() {
        whenever(basePrefs.getString(aKey, aString)).thenReturn(anotherString)
        Assert.assertEquals("getAny() should delegate to getString() when default value is a String",
                anotherString, prefs.getAny(aKey, aString))
    }

    @Test
    fun getAnyLong() {
        whenever(basePrefs.getLong(aKey, aLong)).thenReturn(anotherLong)
        Assert.assertEquals("getAny() should delegate to getLong() when default value is a Long",
                anotherLong, prefs.getAny(aKey, aLong))
    }

    @Test
    fun getAnyInt() {
        whenever(basePrefs.getInt(aKey, anInt)).thenReturn(anotherInt)
        Assert.assertEquals("getAny() should delegate to getInt() when default value is a Int",
                anotherInt, prefs.getAny(aKey, anInt))
    }

    @Test
    fun getAnyBoolean() {
        whenever(basePrefs.getBoolean(aKey, aBoolean)).thenReturn(anotherBoolean)
        Assert.assertEquals("getAny() should delegate to getBoolean() when default value is a Boolean",
                anotherBoolean, prefs.getAny(aKey, aBoolean))
    }

    @Test
    fun getAnyFloat() {
        whenever(basePrefs.getFloat(aKey, aFloat)).thenReturn(anotherFloat)
        Assert.assertEquals("getAny() should delegate to getFloat() when default value is a Float",
                anotherFloat, prefs.getAny(aKey, aFloat))
    }

    @Test
    fun getAnyGroup() {
        val partialMock = mock(ExtSharedPreferencesImpl::class.java)
        whenever(partialMock.getAny(aKey, aGroup)).thenCallRealMethod()
        whenever(partialMock.getEnum(aKey, aGroup)).thenReturn(anotherGroup)
        Assert.assertEquals("getAny() should delegate to getEnum() when default value is a Group",
                anotherGroup, partialMock.getAny(aKey, aGroup))
    }

    @Test
    fun getAnyCallout() {
        val partialMock = mock(ExtSharedPreferencesImpl::class.java)
        whenever(partialMock.getAny(aKey, aCallout)).thenCallRealMethod()
        whenever(partialMock.getEnum(aKey, aCallout)).thenReturn(anotherCallout)
        Assert.assertEquals("getAny() should delegate to getEnum() when default value is a Callout",
                anotherCallout, partialMock.getAny(aKey, aCallout))
    }

    @Test(expected = ClassCastException::class)
    fun getAnyMismatchedType() {
        whenever(basePrefs.getString(aKey, aString)).then { throw ClassCastException() }
        prefs.getAny(aKey, aString)
    }

    @Test(expected = IllegalArgumentException::class)
    fun getAnyUnsupportedType() {
        prefs.getAny(aKey, aClass)
    }

    @Test
    fun putAnyString() {
        whenever(baseEditor.putString(aKey, aString)).thenReturn(baseEditor)
        editor.putAny(aKey, aString)
        verify(baseEditor).putString(aKey, aString)
    }

    @Test
    fun putAnyLong() {
        whenever(baseEditor.putLong(aKey, aLong)).thenReturn(baseEditor)
        editor.putAny(aKey, aLong)
        verify(baseEditor).putLong(aKey, aLong)
    }

    @Test
    fun putAnyInt() {
        whenever(baseEditor.putInt(aKey, anInt)).thenReturn(baseEditor)
        editor.putAny(aKey, anInt)
        verify(baseEditor).putInt(aKey, anInt)
    }

    @Test
    fun putAnyBoolean() {
        whenever(baseEditor.putBoolean(aKey, aBoolean)).thenReturn(baseEditor)
        editor.putAny(aKey, aBoolean)
        verify(baseEditor).putBoolean(aKey, aBoolean)
    }

    @Test
    fun putAnyFloat() {
        whenever(baseEditor.putFloat(aKey, aFloat)).thenReturn(baseEditor)
        editor.putAny(aKey, aFloat)
        verify(baseEditor).putFloat(aKey, aFloat)
    }

    @Test
    fun putAnyGroup() {
        val partialMock = mock(ExtEditorImpl::class.java)
        whenever(partialMock.putAny(aKey, aGroup)).thenCallRealMethod()
        whenever(partialMock.putEnum(aKey, aGroup)).thenReturn(partialMock)
        partialMock.putAny(aKey, aGroup)
        verify(partialMock).putEnum(aKey, aGroup)
    }

    @Test
    fun putAnyCallout() {
        val partialMock = mock(ExtEditorImpl::class.java)
        whenever(partialMock.putAny(aKey, aCallout)).thenCallRealMethod()
        whenever(partialMock.putEnum(aKey, aCallout)).thenReturn(partialMock)
        partialMock.putAny(aKey, aCallout)
        verify(partialMock).putEnum(aKey, aCallout)
    }

    @Test(expected = IllegalArgumentException::class)
    fun putAnyUnsupportedType() {
        editor.putAny(aKey, aClass)
    }

}