package com.simprints.fingerprint.controllers.core.androidResources

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable

class FingerprintAndroidResourcesHelperImpl(val context: Context) : FingerprintAndroidResourcesHelper {

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

    override fun getString(res: Int): String {
        return context.getString(res)
    }
    override fun getStringArray(res: Int): Array<String> = context.resources.getStringArray(res)
    override fun getString(resId: Int, params: Array<Any>): String = context.getString(resId, params)
    override fun getDrawable(res: Int): Drawable? = context.getDrawable(res)
    override fun getStringPlural(stringQuantityKey: Int, quantity: Int, params: Array<Any>): String =
        getStringPlural(context, stringQuantityKey, quantity, params)

    companion object {

        private fun intToQuantity(quantity: Int): QUANTITY {
            when (quantity) {
                0 -> return QUANTITY.ZERO
                1 -> return QUANTITY.ONE
                2 -> return QUANTITY.TWO
                3, 4 -> return QUANTITY.FEW
            }

            return if (quantity > 4) QUANTITY.MANY else QUANTITY.OTHER
        }

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
}
