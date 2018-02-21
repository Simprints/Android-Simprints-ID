package com.simprints.id.tools.roboletric

import android.content.Intent
import com.simprints.id.activities.checkLogin.CheckLoginActivity
import com.simprints.id.activities.launch.LaunchActivity
import com.simprints.id.activities.login.LoginActivity
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

inline fun createRoboCheckLoginViewActivity(startIntent: Intent = Intent()): ActivityController<CheckLoginActivity> {
    return Robolectric.buildActivity(CheckLoginActivity::class.java, startIntent).create()
}

inline fun createRoboLoginActivity(): ActivityController<LoginActivity> {
    return Robolectric.buildActivity(LoginActivity::class.java).create()
}

inline fun createRoboLaunchActivity(): ActivityController<LaunchActivity> {
    return Robolectric.buildActivity(LaunchActivity::class.java).create()
}
