package com.simprints.clientapi.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.rule.ActivityTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import kotlin.reflect.KClass

open class BaseActivityTest<T: AppCompatActivity>(activityClass: KClass<T>,
                                                  private val autoLaunch: Boolean = true) {

    @Rule
    @JvmField
    val rule = if (autoLaunch) {
        IntentsTestRule(activityClass.java, INITIAL_TOUCH_MODE, LAUNCH_ACTIVITY)
    } else {
        ActivityTestRule(activityClass.java, INITIAL_TOUCH_MODE, LAUNCH_ACTIVITY)
    }

    @Before
    fun setUp() {
        if (autoLaunch)
            launch(intent())
        else
            Intents.init()
    }

    @After
    fun tearDown() {
        if (!autoLaunch)
            Intents.release()
    }

    open fun intent() = Intent()

    private fun launch(intent: Intent) {
        rule.launchActivity(intent)
    }

    companion object {
        private const val INITIAL_TOUCH_MODE = true
        private const val LAUNCH_ACTIVITY = false
    }

}
