package com.simprints.id.data.prefs.improvedSharedPreferences

import android.content.SharedPreferences

/**
 * Note: Extending the SharedPreferences interface has benefits over using extension functions:
 * - More testable: very simply approximated, Kotlin extension functions are compiled into
 * static methods. Static methods are a pain to mock, which makes any code that calls
 * extension functions hard to unit test.
 *
 * @author: Etienne Thiery (etienne@simprints.com)
 */
interface ImprovedSharedPreferences : SharedPreferences {

    /**
     * Retrieve a Long, String, Int, Boolean, Float, GROUP or Callout value from the preferences
     *
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     *
     * @return Returns the preference value if it exists, or defValue.
     * Throws ClassCastException if there is a preference with this name that is not the same type as
     * defValue.
     * Throws IllegalArgumentException if defValue is an unsupported type.
     */
    fun <T: Any> getAny(key: String, defValue: T): T

    /**
     * Create a new Editor for these preferences, through which you can make
     * modifications to the data in the preferences and atomically commit those
     * changes back to the ImprovedSharedPreferences object.
     *
     * <p>Note that you <em>must</em> call {@link Editor#commit} or {@link Editor#apply} to have any
     * changes you perform in the Editor actually show up in the ImprovedSharedPreferences.
     *
     * @return Returns a new instance of the {@link Editor} interface, allowing
     * you to modify the values in this ImprovedSharedPreferences object.
     */
    override fun edit(): Editor

    /**
     * Interface used for modifying values in an {@link ImprovedSharedPreferences}
     * object.  All changes you make in an editor are batched, and not copied
     * back to the original {@link ImprovedSharedPreferences} until you call {@link #commit}
     * or {@link #apply}
     */
    interface Editor: SharedPreferences.Editor {

        /**
         * Set a Long, String, Int, Boolean, Float, GROUP or Callout value in the preferences editor,
         * to be written back once {@link #commit} or {@link #apply} are called.
         *
         * @param key The name of the preference to modify.
         * @param value The new value for the preference.
         *
         * @return Returns a reference to the same Editor object, so you can chain put calls together.
         */
        fun <T: Any> putAny(key: String, value: T): Editor

    }

}