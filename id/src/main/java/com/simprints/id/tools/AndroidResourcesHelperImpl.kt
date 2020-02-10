package com.simprints.id.tools

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.PluralsRes
import androidx.core.content.ContextCompat

class AndroidResourcesHelperImpl(val context: Context) : AndroidResourcesHelper {

    /**
     * The abstract concept of quantity found in most languages. Note that this will not be correct
     * for all, more complicated, languages (e.g. Russian). Include only the necessary strings for
     * the relevant translation of strings.xml, but always include "other" for all languages.
     * For English, it usually sufficient to include only "one" and "other" (and sometimes "zero").
     *
     * See more: https://developer.android.com/guide/topics/resources/string-resource.html#Plurals
     */
    private enum class QUANTITY constructor(private val title: String) {
        ZERO("zero"),
        ONE("one"),
        TWO("two"),
        FEW("few"),
        MANY("many"),
        OTHER("other");

        override fun toString(): String {
            return title
        }
    }

    companion object {

        /**
         * Converts an integer to the grammatical concept of quantity. This is an incomplete conversion
         * but works for most languages. More advanced processing is required for languages that treat
         * numbers that end in zero, one, and two differently (e.g. 22 in Russian).
         *
         * @param quantity The integer number that describes the quantity
         * @return The heuristic description of the number
         */
        private fun intToQuantity(quantity: Int): QUANTITY {
            when (quantity) {
                0 -> return QUANTITY.ZERO
                1 -> return QUANTITY.ONE
                2 -> return QUANTITY.TWO
                3, 4 -> return QUANTITY.FEW
            }

            return if (quantity > 4) QUANTITY.MANY else QUANTITY.OTHER
        }

        /**
         * A translation-friendly way to access pluralised string resources. Some assumptions are made
         * about the names of the child string resources. Always make sure there is an "other" version
         * of the plural as this will be used as the default. Besides "other", only include plurals in
         * strings.xml that are required by the language.
         *
         * @param context The parent context
         * @param stringQuantityKey The R.string id of the key of the target resource
         * @param quantity The number to use in deciding which plural to use
         * @param values The values used to format the string
         * @return The formatted final string
         */
        @JvmStatic
        fun getStringPlural(context: Context, stringQuantityKey: Int, quantity: Int, params: Array<Any>): String {
            val res = context.resources

            return try {
                val targetStringResourceName = res.getString(stringQuantityKey) + "_" + intToQuantity(quantity).toString()
                val targetStringResourceId = res.getIdentifier(targetStringResourceName, "string", context.packageName)
                res.getString(targetStringResourceId, *params)
            } catch (e: Resources.NotFoundException) {
                // If we can't find the resource, try instead to find the "other" version
                val targetStringResourceName = res.getString(stringQuantityKey) + "_" + QUANTITY.OTHER.toString()
                val targetStringResourceId = res.getIdentifier(targetStringResourceName, "string", context.packageName)
                res.getString(targetStringResourceId, *params)
            }
        }
    }

    override fun getStringPlural(stringQuantityKey: Int, quantity: Int, params: Array<Any>): String =
        getStringPlural(context, stringQuantityKey, quantity, params)

    override fun getQuantityString(
        @PluralsRes resId: Int,
        quantity: Int,
        params: Array<Any>
    ): String = context.resources.getQuantityString(resId, quantity, params)

    override fun getQuantityString(@PluralsRes resId: Int, quantity: Int): String {
        return context.resources.getQuantityString(resId, quantity)
    }

    override fun getString(res: Int): String = context.getString(res)
    override fun getString(resId: Int, params: Array<Any>): String = context.getString(resId, *params)
    override fun getStringArray(res: Int): Array<String> = context.resources.getStringArray(res)
    override fun getDrawable(res: Int): Drawable? = ContextCompat.getDrawable(context, res)
    override fun getColorStateList(color: Int): ColorStateList? = ContextCompat.getColorStateList(context, color)

    override fun getColour(colourId: Int): Int = ContextCompat.getColor(context, colourId)
}
