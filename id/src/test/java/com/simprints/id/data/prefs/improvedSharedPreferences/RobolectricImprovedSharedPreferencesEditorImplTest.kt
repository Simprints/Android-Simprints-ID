package com.simprints.id.data.prefs.improvedSharedPreferences

import android.content.Context
import android.content.SharedPreferences
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

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
class RobolectricImprovedSharedPreferencesEditorImplTest {

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
    private lateinit var editor: ImprovedSharedPreferences.Editor

    @Before
    fun setUp() {
        basePrefs = RuntimeEnvironment.application
                .getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)
        baseEditor = basePrefs.edit()
        editor = ImprovedSharedPreferencesEditorImpl(baseEditor)
    }

    @Test
    fun putAnyString() {
        editor.putAny(key, stringValue).apply()
        Assert.assertEquals(stringValue, basePrefs.getString(key, defStringValue))
    }

    @Test
    fun putAnyLong() {
        editor.putAny(key, longValue).apply()
        Assert.assertEquals(longValue, basePrefs.getLong(key, defLongValue))
    }

    @Test
    fun putAnyInt() {
        editor.putAny(key, intValue).apply()
        Assert.assertEquals(intValue, basePrefs.getInt(key, defIntValue))
    }

    @Test
    fun putAnyBoolean() {
        editor.putAny(key, booleanValue).apply()
        Assert.assertEquals(booleanValue, basePrefs.getBoolean(key, defBooleanValue))
    }

    @Test
    fun putAnyFloat() {
        editor.putAny(key, floatValue).apply()
        Assert.assertEquals(floatValue, basePrefs.getFloat(key, defFloatValue))
    }

    @Test(expected = IllegalArgumentException::class)
    fun putAnyUnsupportedType() {
        editor.putAny(key, classValue)
    }

}