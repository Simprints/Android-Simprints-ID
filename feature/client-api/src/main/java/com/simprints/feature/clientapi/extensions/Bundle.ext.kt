package com.simprints.feature.clientapi.extensions

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simprints.feature.clientapi.mappers.response.LibSimprintsResponseMapper

internal fun Bundle.toMap(): Map<String, Any> {
    val map = HashMap<String, Any>()
    keySet().forEach { map[it] = get(it) ?: "" }

    return map
}

internal fun Map<String, Any>.extractString(key: String) = getOrElse(key) { "" } as String

fun Bundle.getResultCodeFromExtras(): Int {
    val resultCode = getInt(LibSimprintsResponseMapper.RESULT_CODE_OVERRIDE, AppCompatActivity.RESULT_OK)
    remove(LibSimprintsResponseMapper.RESULT_CODE_OVERRIDE)
    return resultCode
}
