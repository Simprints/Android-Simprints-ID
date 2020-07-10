package com.simprints.core.tools.extentions

import android.content.Context
import android.widget.Toast
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.simprints.core.tools.utils.QuantityHelper

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showToast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, this.resources.getText(resId), duration).show()
}

fun Context.getStringPlural(stringQuantityKey: Int, quantity: Int, params: Array<Any>): String =
    QuantityHelper.getStringPlural(this, stringQuantityKey, quantity, params)

fun Context.getQuantityString(
    @PluralsRes resId: Int,
    quantity: Int,
    params: Array<Any>
): String = this.resources.getQuantityString(resId, quantity, params)

fun Context.getQuantityString(@PluralsRes resId: Int, quantity: Int): String {
    return resources.getQuantityString(resId, quantity)
}

fun Context.getStringArray(res: Int): Array<String> = resources.getStringArray(res)
