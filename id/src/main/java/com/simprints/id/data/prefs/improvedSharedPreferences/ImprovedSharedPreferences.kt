package com.simprints.id.data.prefs.improvedSharedPreferences

import com.simprints.id.exceptions.unexpected.MismatchedTypeError
import com.simprints.id.exceptions.unexpected.NonPrimitiveTypeError

/**
 * Extension of the SharedPreferences interface of the Android framework.
 * Adds generic read and write capabilities, and provides a more test friendly boundary.
 */
interface ImprovedSharedPreferences {

    /**
     * Retrieve a value of primitive type ([Byte], [Short], [Int], [Long], [Float], [Double],
     * [String] or [Boolean]) from the preferences.
     *
     * Return the value if it exists, or the provided default value if it does not.
     *
     * @throws [NonPrimitiveTypeError] if T is not a primitive type.
     * @throws [MismatchedTypeError] if an exception occurred during retrieval due to the
     * stored value type being different from the requested type.
     */
    fun <T : Any> getPrimitive(key: String, defaultValue: T): T

    /**
     * Create a new [ImprovedSharedPreferences.Editor] for these preferencesManager, through which you can
     * make modifications to the data in the preferencesManager and atomically commit those changes back
     * to the preferencesManager.
     *
     * Note that you must call [Editor.commit] or [Editor.apply] to have any
     * changes you perform in the Editor actually show up in the preferencesManager.
     */
    fun edit(): ImprovedSharedPreferences.Editor

    fun getString(key: String, defaultValue: String): String

    /**
     * Interface used for modifying values in preferencesManager. All changes you make in an editor are
     * batched, and will not actually show up in the preferencesManager until you call [Editor.commit] or
     * [Editor.apply]
     */
    interface Editor {

        /**
         * Set a value of primitive type ([Byte], [Short], [Int], [Long], [Float], [Double],
         * [String] or [Boolean]) in the preferences editor, to be written back once [Editor.commit]
         * or [Editor.apply] are called.
         *
         * Return a reference to the same [ImprovedSharedPreferences.Editor] object, so you can
         * chain put calls together.
         *
         * Throw a [NonPrimitiveTypeError] if T is not a primitive type.
         */
        fun <T : Any> putPrimitive(key: String, value: T): ImprovedSharedPreferences.Editor

        fun commit()

        fun apply()
    }
}
