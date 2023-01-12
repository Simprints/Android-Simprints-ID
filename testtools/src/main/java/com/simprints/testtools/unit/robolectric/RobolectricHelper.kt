package com.simprints.testtools.unit.robolectric

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.junit.Assert.assertEquals
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowActivity

inline fun <reified T : Activity> createActivity(startIntent: Intent? = null): ActivityController<T> =
    startIntent?.let {
        Robolectric.buildActivity(T::class.java, it).create()
    } ?: Robolectric.buildActivity(T::class.java).create()

fun assertActivityStarted(clazz: Class<out Activity>, activity: AppCompatActivity) {
    val shadowActivity = Shadows.shadowOf(activity)
    assertActivityStarted(clazz, shadowActivity)
}

fun assertActivityStarted(clazz: Class<out Activity>, shadowActivity: ShadowActivity) {
    val startedIntent = shadowActivity.nextStartedActivity
    assertActivityStarted(clazz, startedIntent)
}

fun assertActivityStarted(clazz: Class<out Activity>, intent: Intent) {
    assertActivityStarted(clazz.name, intent)
}

fun assertActivityStarted(clazzName: String, intent: Intent) {
    assertEquals(clazzName, intent.component?.className)
}

inline fun <reified T : Activity> createAndStartActivity(extras: Bundle? = null): T {
    val intent = Intent().apply {
        extras?.let { putExtras(it) }
    }

    val controller = createActivity<T>(intent)
    controller.resume().visible()

    return controller.get()
}
