package com.simprints.id.tools.roboletric

import android.content.Intent
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.activities.checkLogin.openedByMainLauncher.CheckLoginFromMainLauncherActivity
import com.simprints.id.activities.launch.LaunchActivity
import com.simprints.id.activities.login.LoginActivity
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

inline fun createRoboCheckLoginFromIntentViewActivity(startIntent: Intent = Intent()): ActivityController<CheckLoginFromIntentActivity> {
    return Robolectric.buildActivity(CheckLoginFromIntentActivity::class.java, startIntent).create()
}

inline fun createRoboCheckLoginMainLauncherAppActivity(): ActivityController<CheckLoginFromMainLauncherActivity> {
    return Robolectric.buildActivity(CheckLoginFromMainLauncherActivity::class.java).create()
}

inline fun createRoboLoginActivity(startIntent: Intent = Intent()): ActivityController<LoginActivity> {
    return Robolectric.buildActivity(LoginActivity::class.java, startIntent).create()
}

inline fun createRoboLaunchActivity(): ActivityController<LaunchActivity> {
    return Robolectric.buildActivity(LaunchActivity::class.java).create()
}
