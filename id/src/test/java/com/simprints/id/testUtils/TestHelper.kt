package com.simprints.id.testUtils

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowActivity

inline fun assertActivityStarted(clazz: Class<out Activity>, activity: AppCompatActivity) {
    val shadowActivity = Shadows.shadowOf(activity)
    assertActivityStarted(clazz, shadowActivity)
}

inline fun assertActivityStarted(clazz: Class<out Activity>, shadowActivity: ShadowActivity) {
    val startedIntent = shadowActivity.nextStartedActivity
    assertActivityStarted(clazz, startedIntent)
}

inline fun assertActivityStarted(clazz: Class<out Activity>, intent: Intent) {
    Assert.assertThat(intent.component.className,
        CoreMatchers.equalTo(clazz.name))
}
