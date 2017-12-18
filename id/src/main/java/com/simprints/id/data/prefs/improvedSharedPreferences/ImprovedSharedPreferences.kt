package com.simprints.id.data.prefs.improvedSharedPreferences

import android.content.SharedPreferences

/**
 * Extension of the SharedPreferences interface of the Android framework.
 * Adds generic read and write capabilities, and provides a more test friendly boundary.
 *
 * @author: Etienne Thiery (etienne@simprints.com)
 */
interface ImprovedSharedPreferences : SharedPreferences {

    /**
     * Retrieve a value of primitive type ([Byte], [Short], [Int], [Long], [Float], [Double],
     * [String] or [Boolean]) from the preferences.
     *
     * Return the value if it exists, or the provided default value if it does not.
     *
     * Throw a [NonPrimitiveTypeException] if T is not a primitive type.
     * Throw a [MismatchedTypeException] if an exception occurred during retrieval due to the
     * stored value type being different from the requested type.
     */
    fun <T: Any> getPrimitive(key: String, defaultValue: T): T

    /**
     * Create a new [ImprovedSharedPreferences.Editor] for these preferences, through which you can
     * make modifications to the data in the preferences and atomically commit those changes back
     * to the preferences.
     *
     * Note that you must call [Editor.commit] or [Editor.apply] to have any
     * changes you perform in the Editor actually show up in the preferences.
     */
    override fun edit(): ImprovedSharedPreferences.Editor

    /**
     * Interface used for modifying values in preferences. All changes you make in an editor are
     * batched, and will not actually show up in the preferences until you call [Editor.commit] or
     * [Editor.apply]
     */
    interface Editor: SharedPreferences.Editor {

        /**
         * Set a value of primitive type ([Byte], [Short], [Int], [Long], [Float], [Double],
         * [String] or [Boolean]) in the preferences editor, to be written back once [Editor.commit]
         * or [Editor.apply] are called.
         *
         * Return a reference to the same [ImprovedSharedPreferences.Editor] object, so you can
         * chain put calls together.
         *
         * Throw a [NonPrimitiveTypeException] if T is not a primitive type.
         */
        fun <T: Any> putPrimitive(key: String, value: T): ImprovedSharedPreferences.Editor

    }

}