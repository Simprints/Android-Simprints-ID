package com.simprints.core.tools.extentions

import android.content.Context
import android.util.TypedValue
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import java.nio.ByteBuffer
import java.nio.ByteOrder

@ExcludedFromGeneratedTestCoverageReports("UI code")
fun Float.dpToPx(context: Context) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics)

fun FloatArray.toBytes(): ByteArray {
    val bytes = ByteArray(size * Float.SIZE_BYTES)
    val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
    buffer.asFloatBuffer().put(this)
    return bytes
}

fun ByteArray.toFloats(): FloatArray {
    val floats = FloatArray(size / Float.SIZE_BYTES)
    val buffer = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN)
    buffer.asFloatBuffer().get(floats)
    return floats
}
