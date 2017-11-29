package com.simprints.id.data.prefs.improvedSharedPreferences

import android.content.Context
import android.content.SharedPreferences
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.mockito.Mockito.`when` as whenever

// TODO: Figure out why Roboletric is not happy when running tests with code coverage

/**
 * At first, I implemented getAny() and putAny() as extension functions of the SharedPreferences
 * interface.
 *
 * I had trouble mocking SharedPreferences to check that these functions would delegate their work
 * to the right SharedPreferences methods, so I used Robolectric.
 *
 * It allows doing "black box" testing, that is test ImprovedSharedPreferences without making any
 * assumption about the underlying implementation.
 *
 * @author: Etienne Thiery (etienne@simprints.com)
 */
@RunWith(RobolectricTestRunner::class)
class RobolectricImprovedSharedPreferencesImplTest {

    companion object {

        val sharedPrefName = "aSharedPrefName"

        val key = "key"

        val defStringValue = "defValue"
        val stringValue = "value"

        val defLongValue = 0L
        val longValue = 1L

        val defIntValue = 0
        val intValue = 1

        val defBooleanValue = false
        val booleanValue = true

        val defFloatValue = 0f
        val floatValue = 1f

        val classValue = RobolectricImprovedSharedPreferencesImplTest::class
    }

    private lateinit var basePrefs: SharedPreferences
    private lateinit var baseEditor: SharedPreferences.Editor
    private lateinit var prefs: ImprovedSharedPreferences

    @Before
    fun setUp() {
        basePrefs = RuntimeEnvironment.application
                .getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)
        baseEditor = basePrefs.edit()
        prefs = ImprovedSharedPreferencesImpl(basePrefs)
    }


    @Test
    fun getAnyNonExistentString() {
        Assert.assertEquals(defStringValue, prefs.getAny(key, defStringValue))
    }

    @Test
    fun getAnyExistentString() {
        baseEditor.putString(key, stringValue).apply()
        Assert.assertEquals(stringValue, prefs.getAny(key, defStringValue))
    }

    @Test
    fun getAnyNonExistentLong() {
        Assert.assertEquals(defLongValue, prefs.getAny(key, defLongValue))
    }

    @Test
    fun getAnyExistentLong() {
        baseEditor.putLong(key, longValue).apply()
        Assert.assertEquals(longValue, prefs.getAny(key, defLongValue))
    }

    @Test
    fun getAnyNonExistentInt() {
        Assert.assertEquals(defIntValue, prefs.getAny(key, defIntValue))
    }

    @Test
    fun getAnyExistentInt() {
        baseEditor.putInt(key, intValue).apply()
        Assert.assertEquals(intValue, prefs.getAny(key, defIntValue))
    }

    @Test
    fun getAnyNonExistentBoolean() {
        Assert.assertEquals(defBooleanValue, prefs.getAny(key, defBooleanValue))
    }

    @Test
    fun getAnyExistentBoolean() {
        baseEditor.putBoolean(key, booleanValue).apply()
        Assert.assertEquals(booleanValue, prefs.getAny(key, defBooleanValue))
    }

    @Test
    fun getAnyNonExistentFloat() {
        Assert.assertEquals(defFloatValue, prefs.getAny(key, defFloatValue))
    }

    @Test
    fun getAnyExistentFloat() {
        baseEditor.putFloat(key, floatValue).apply()
        Assert.assertEquals(floatValue, prefs.getAny(key, defFloatValue))
    }

    @Test(expected = ClassCastException::class)
    fun getAnyMismatchedType() {
        baseEditor.putFloat(key, floatValue).apply()
        prefs.getAny(key, defIntValue)
    }

    @Test(expected = IllegalArgumentException::class)
    fun getAnyUnsupportedType() {
        prefs.getAny(key, classValue)
    }

}