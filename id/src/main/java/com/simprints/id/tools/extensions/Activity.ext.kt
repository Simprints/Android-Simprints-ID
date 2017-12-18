package com.simprints.id.tools.extensions

import android.app.Activity
import android.content.pm.PackageManager

val Activity.activityName: String
    get() =
        try {
            packageManager.getActivityInfo(componentName, 0).name
        } catch (e: PackageManager.NameNotFoundException) {
            "Class activityName not found"
        }
