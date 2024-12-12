package com.simprints.infra.uibase.system

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports

object Clipboard {
    @ExcludedFromGeneratedTestCoverageReports("Framework wrapper code")
    fun copyToClipboard(
        context: Context,
        text: String,
    ) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(text, text)
        clipboard.setPrimaryClip(clip)
    }
}
