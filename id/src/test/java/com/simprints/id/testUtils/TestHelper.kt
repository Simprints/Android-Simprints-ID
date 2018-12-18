package com.simprints.id.testUtils

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import junit.framework.TestCase.assertEquals
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowActivity

fun assertActivityStarted(clazz: Class<out Activity>, activity: AppCompatActivity) {
    val shadowActivity = Shadows.shadowOf(activity)
    assertActivityStarted(clazz, shadowActivity)
}

fun assertActivityStarted(clazz: Class<out Activity>, shadowActivity: ShadowActivity) {
    val startedIntent = shadowActivity.nextStartedActivity
    assertActivityStarted(clazz, startedIntent)
}

fun assertActivityStarted(clazz: Class<out Activity>, intent: Intent) {
    assertEquals(intent.component?.className, clazz.name)
}
