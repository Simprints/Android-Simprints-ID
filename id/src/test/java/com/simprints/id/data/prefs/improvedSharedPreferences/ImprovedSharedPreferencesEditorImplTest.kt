package com.simprints.id.data.prefs.improvedSharedPreferences

import android.content.SharedPreferences
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

/**
 * @author: Etienne Thiery (etienne@simprints.com)
 */
class ImprovedSharedPreferencesEditorImplTest {

    companion object {
        val aKey = "key"
        val aString = "0"
        val aLong = 0L
        val anInt = 0
        val aBoolean = false
        val aFloat = 0f
        val aClass = ImprovedSharedPreferencesImplTest::class
    }

    private lateinit var baseEditor: SharedPreferences.Editor
    private lateinit var editor: ImprovedSharedPreferences.Editor

    @Before
    fun setUp() {
        baseEditor = Mockito.mock(SharedPreferences.Editor::class.java)
        editor = ImprovedSharedPreferencesEditorImpl(baseEditor)
    }


    @Test
    fun putAnyString() {
        Mockito.`when`(baseEditor.putString(aKey, aString)).thenReturn(baseEditor)
        editor.putAny(aKey, aString)
        Mockito.verify(baseEditor).putString(aKey, aString)
    }

    @Test
    fun putAnyLong() {
        Mockito.`when`(baseEditor.putLong(aKey, aLong)).thenReturn(baseEditor)
        editor.putAny(aKey, aLong)
        Mockito.verify(baseEditor).putLong(aKey, aLong)
    }

    @Test
    fun putAnyInt() {
        Mockito.`when`(baseEditor.putInt(aKey, anInt)).thenReturn(baseEditor)
        editor.putAny(aKey, anInt)
        Mockito.verify(baseEditor).putInt(aKey, anInt)
    }

    @Test
    fun putAnyBoolean() {
        Mockito.`when`(baseEditor.putBoolean(aKey, aBoolean)).thenReturn(baseEditor)
        editor.putAny(aKey, aBoolean)
        Mockito.verify(baseEditor).putBoolean(aKey, aBoolean)
    }

    @Test
    fun putAnyFloat() {
        Mockito.`when`(baseEditor.putFloat(aKey, aFloat)).thenReturn(baseEditor)
        editor.putAny(aKey, aFloat)
        Mockito.verify(baseEditor).putFloat(aKey, aFloat)
    }

    @Test(expected = IllegalArgumentException::class)
    fun putAnyUnsupportedType() {
        editor.putAny(aKey, aClass)
    }

}