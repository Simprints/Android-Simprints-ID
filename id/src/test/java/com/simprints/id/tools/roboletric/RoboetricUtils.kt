package com.simprints.id.tools.roboletric

import android.app.Activity
import org.robolectric.android.controller.ActivityController
import org.robolectric.android.controller.ComponentController


@Throws(NoSuchFieldException::class, IllegalAccessException::class)
fun replaceComponentInActivityController(activityController: ActivityController<*>, activity: Activity) {
    val componentField = ComponentController::class.java.getDeclaredField("component")
    componentField.isAccessible = true
    componentField.set(activityController, activity)
}
