package com.simprints.fingerprint.controllers.core.androidResources

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.simprints.core.tools.LanguageHelper
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.AndroidResourcesHelperImpl

class FingerprintAndroidResourcesHelperImpl(private val context: Context, private val coreAndroidResourcesHelper: AndroidResourcesHelper) : FingerprintAndroidResourcesHelper {

    private val languageContext by lazy {
        context.createConfigurationContext(coreAndroidResourcesHelper.getLocaleConfiguration())
    }

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
        return coreAndroidResourcesHelper.getString(res)
    }
    override fun getStringArray(res: Int): Array<String> = coreAndroidResourcesHelper.getStringArray(res)
    override fun getString(resId: Int, params: Array<Any>): String = coreAndroidResourcesHelper.getString(resId, params)
    override fun getDrawable(res: Int): Drawable? = coreAndroidResourcesHelper.getDrawable(res)
    override fun getStringPlural(stringQuantityKey: Int, quantity: Int, params: Array<Any>): String =
        coreAndroidResourcesHelper.getStringPlural(stringQuantityKey, quantity, params)

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
