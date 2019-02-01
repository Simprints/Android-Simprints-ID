package com.simprints.testframework.unit

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

object RobolectricHelper {

    inline fun <reified T : Activity> createActivity(startIntent: Intent? = null): ActivityController<T> =
        startIntent?.let {
            Robolectric.buildActivity(T::class.java, it).create()
        } ?: Robolectric.buildActivity(T::class.java).create()

    fun getSharedPreferences(fileName: String): SharedPreferences =
        ApplicationProvider.getApplicationContext<Application>().getSharedPreferences(fileName, Context.MODE_PRIVATE)
}
