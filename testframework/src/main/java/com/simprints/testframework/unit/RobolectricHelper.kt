package com.simprints.testframework.unit

import android.app.Activity
import android.content.Intent
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

object RobolectricHelper {

    inline fun <reified T : Activity> createActivity(startIntent: Intent? = null): ActivityController<T> =
        startIntent?.let {
            Robolectric.buildActivity(T::class.java, it).create()
        } ?: Robolectric.buildActivity(T::class.java).create()
}
