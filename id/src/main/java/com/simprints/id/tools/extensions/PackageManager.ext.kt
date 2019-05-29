package com.simprints.id.tools.extensions

import android.content.Intent
import android.content.pm.PackageManager

fun PackageManager.scannerAppIntent(): Intent {
    val intent = Intent("com.google.zxing.client.android.SCAN")
    intent.putExtra("SAVE_HISTORY", false)
    intent.putExtra("SCAN_MODE", "QR_CODE_MODE")
    return intent
}
