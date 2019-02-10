package com.simprints.testframework.unit.robolectric

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import junit.framework.TestCase
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
    TestCase.assertEquals(intent.component?.className, clazz.name)
}

fun <T : Activity> ActivityController<T>.showOnScreen(): ActivityController<T> = this.start().resume().visible()

fun getSharedPreferences(fileName: String): SharedPreferences =
    ApplicationProvider.getApplicationContext<Application>().getSharedPreferences(fileName, Context.MODE_PRIVATE)
