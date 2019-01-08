package com.simprints.id.testUtils.roboletric

import android.content.Intent
import com.simprints.id.activities.alert.AlertActivity
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.activities.checkLogin.openedByMainLauncher.CheckLoginFromMainLauncherActivity
import com.simprints.id.activities.launch.LaunchActivity
import com.simprints.id.activities.login.LoginActivity
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

fun createRoboCheckLoginFromIntentViewActivity(startIntent: Intent = Intent()): ActivityController<CheckLoginFromIntentActivity> =
    Robolectric.buildActivity(CheckLoginFromIntentActivity::class.java, startIntent).create()

fun createRoboCheckLoginMainLauncherAppActivity(): ActivityController<CheckLoginFromMainLauncherActivity> =
    Robolectric.buildActivity(CheckLoginFromMainLauncherActivity::class.java).create()

fun createRoboLoginActivity(startIntent: Intent = Intent()): ActivityController<LoginActivity> =
    Robolectric.buildActivity(LoginActivity::class.java, startIntent).create()

fun createRoboAlertActivity(startIntent: Intent = Intent()): ActivityController<AlertActivity> =
    Robolectric.buildActivity(AlertActivity::class.java, startIntent).create()

fun createRoboLaunchActivity(): ActivityController<LaunchActivity> =
    Robolectric.buildActivity(LaunchActivity::class.java).create()

