package com.simprints.id.data.prefs.improvedSharedPreferences

import android.content.SharedPreferences
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.Mockito.`when` as whenever

/**
 * @author: Etienne Thiery (etienne@simprints.com)
 */
class ImprovedSharedPreferencesImplTest {

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
        val aClass = ImprovedSharedPreferencesImplTest::class
    }

    private lateinit var basePrefs: SharedPreferences
    private lateinit var prefs: ImprovedSharedPreferences


    @Before
    fun setUp() {
        basePrefs = mock(SharedPreferences::class.java)
        prefs = ImprovedSharedPreferencesImpl(basePrefs)
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

    @Test(expected = ClassCastException::class)
    fun getAnyMismatchedType() {
        whenever(basePrefs.getString(aKey, aString)).then { throw ClassCastException() }
        prefs.getAny(aKey, aString)
    }

    @Test(expected = IllegalArgumentException::class)
    fun getAnyUnsupportedType() {
        prefs.getAny(aKey, aClass)
    }



}