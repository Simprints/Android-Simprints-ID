package com.simprints.id.tools.delegations.sharedPreferences

import android.content.Context
import android.content.SharedPreferences
import com.simprints.id.model.Callout
import com.simprints.libdata.tools.Constants
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.mockito.Mockito.`when` as whenever

// TODO: Figure out why Roboletric is not happy when running tests with code coverage

/**
 * At first, I implemented getAny(), putAny(), getEnum() and putEnum() as extension functions of
 * the SharedPreferences interface.
 * I had trouble mocking SharedPreferences to check that these 4 functions would delegate their work
 * to the right SharedPreferences methods, so I used Robolectric.
 * This is not longer need, but I leave it here for illustration purposes
 */
@RunWith(RobolectricTestRunner::class)
class RobolectricExtSharedPreferencesImplTest {

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

        val defGroupValue = Constants.GROUP.GLOBAL
        val groupValue = Constants.GROUP.USER

        val defCalloutValue = Callout.NULL
        val calloutValue = Callout.IDENTIFY

        val classValue = RobolectricExtSharedPreferencesImplTest::class
    }

    private lateinit var basePrefs: SharedPreferences
    private lateinit var prefs: ExtSharedPreferences

    @Before
    fun setUp() {
        basePrefs = RuntimeEnvironment.application
                .getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)
        prefs = ExtSharedPreferencesImpl(basePrefs)
    }

    @Test
    fun putEnumAndGetEnum() {
        // Note: No assumption can be made about the implementation of putEnum()/getEnum()
        // (is it storing the ordinal of the enum value? its name?)
        // So these two functions cannot be tested separately.
        prefs.edit().putEnum(key, calloutValue).apply()
        Assert.assertEquals(calloutValue, prefs.getEnum(key, defCalloutValue))
    }

    @Test
    fun getNonExistentEnum() {
        Assert.assertEquals(defCalloutValue, prefs.getEnum(key, defCalloutValue))
    }


    @Test
    fun getAnyNonExistentString() {
        Assert.assertEquals(defStringValue, prefs.getAny(key, defStringValue))
    }

    @Test
    fun getAnyExistentString() {
        prefs.edit().putString(key, stringValue).apply()
        Assert.assertEquals(stringValue, prefs.getAny(key, defStringValue))
    }

    @Test
    fun getAnyNonExistentLong() {
        Assert.assertEquals(defLongValue, prefs.getAny(key, defLongValue))
    }

    @Test
    fun getAnyExistentLong() {
        prefs.edit().putLong(key, longValue).apply()
        Assert.assertEquals(longValue, prefs.getAny(key, defLongValue))
    }

    @Test
    fun getAnyNonExistentInt() {
        Assert.assertEquals(defIntValue, prefs.getAny(key, defIntValue))
    }

    @Test
    fun getAnyExistentInt() {
        prefs.edit().putInt(key, intValue).apply()
        Assert.assertEquals(intValue, prefs.getAny(key, defIntValue))
    }

    @Test
    fun getAnyNonExistentBoolean() {
        Assert.assertEquals(defBooleanValue, prefs.getAny(key, defBooleanValue))
    }

    @Test
    fun getAnyExistentBoolean() {
        prefs.edit().putBoolean(key, booleanValue).apply()
        Assert.assertEquals(booleanValue, prefs.getAny(key, defBooleanValue))
    }

    @Test
    fun getAnyNonExistentFloat() {
        Assert.assertEquals(defFloatValue, prefs.getAny(key, defFloatValue))
    }

    @Test
    fun getAnyExistentFloat() {
        prefs.edit().putFloat(key, floatValue).apply()
        Assert.assertEquals(floatValue, prefs.getAny(key, defFloatValue))
    }

    @Test
    fun getAnyNonExistentGroup() {
        Assert.assertEquals(defGroupValue, prefs.getAny(key, defGroupValue))
    }

    @Test
    fun getAnyExistentGroup() {
        prefs.edit().putEnum(key, groupValue).apply()
        Assert.assertEquals(groupValue, prefs.getAny(key, defGroupValue))
    }

    @Test
    fun getAnyNonExistentCallout() {
        Assert.assertEquals(defCalloutValue, prefs.getAny(key, defCalloutValue))
    }

    @Test
    fun getAnyExistentCallout() {
        prefs.edit().putEnum(key, calloutValue).apply()
        Assert.assertEquals(calloutValue, prefs.getAny(key, defCalloutValue))
    }

    @Test(expected = ClassCastException::class)
    fun getAnyMismatchedType() {
        prefs.edit().putFloat(key, floatValue).apply()
        prefs.getAny(key, defIntValue)
    }

    @Test(expected = IllegalArgumentException::class)
    fun getAnyUnsupportedType() {
        prefs.getAny(key, classValue)
    }

    @Test
    fun putAnyString() {
        prefs.edit().putAny(key, stringValue).apply()
        Assert.assertEquals(stringValue, prefs.getString(key, defStringValue))
    }

    @Test
    fun putAnyLong() {
        prefs.edit().putAny(key, longValue).apply()
        Assert.assertEquals(longValue, prefs.getLong(key, defLongValue))
    }

    @Test
    fun putAnyInt() {
        prefs.edit().putAny(key, intValue).apply()
        Assert.assertEquals(intValue, prefs.getInt(key, defIntValue))
    }

    @Test
    fun putAnyBoolean() {
        prefs.edit().putAny(key, booleanValue).apply()
        Assert.assertEquals(booleanValue, prefs.getBoolean(key, defBooleanValue))
    }

    @Test
    fun putAnyFloat() {
        prefs.edit().putAny(key, floatValue).apply()
        Assert.assertEquals(floatValue, prefs.getFloat(key, defFloatValue))
    }

    @Test
    fun putAnyGroup() {
        prefs.edit().putAny(key, groupValue).apply()
        Assert.assertEquals(groupValue, prefs.getEnum(key, defGroupValue))
    }

    @Test
    fun putAnyCallout() {
        prefs.edit().putAny(key, calloutValue).apply()
        Assert.assertEquals(calloutValue, prefs.getEnum(key, defCalloutValue))
    }

    @Test(expected = IllegalArgumentException::class)
    fun putAnyUnsupportedType() {
        prefs.edit().putAny(key, classValue)
    }

}