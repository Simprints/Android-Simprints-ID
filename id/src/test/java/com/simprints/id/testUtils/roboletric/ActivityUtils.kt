package com.simprints.id.testUtils.roboletric

import android.content.Intent
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.activities.checkLogin.openedByMainLauncher.CheckLoginFromMainLauncherActivity
import com.simprints.id.activities.login.LoginActivity
import com.simprints.id.activities.matching.MatchingActivity
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

inline fun createRoboMatchingActivity(startIntent: Intent = Intent()): ActivityController<MatchingActivity> {
    return Robolectric.buildActivity(MatchingActivity::class.java, startIntent).create()
}
