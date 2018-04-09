package com.simprints.id.testUtils.roboletric

import android.content.Intent
import com.simprints.id.activities.about.AboutActivity
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.activities.checkLogin.openedByMainLauncher.CheckLoginFromMainLauncherActivity
import com.simprints.id.activities.login.LoginActivity
import com.simprints.id.activities.matching.MatchingActivity
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

fun createRoboCheckLoginFromIntentViewActivity(startIntent: Intent = Intent()): ActivityController<CheckLoginFromIntentActivity> =
    Robolectric.buildActivity(CheckLoginFromIntentActivity::class.java, startIntent).create()

fun createRoboCheckLoginMainLauncherAppActivity(): ActivityController<CheckLoginFromMainLauncherActivity> =
    Robolectric.buildActivity(CheckLoginFromMainLauncherActivity::class.java).create()

fun createRoboLoginActivity(startIntent: Intent = Intent()): ActivityController<LoginActivity> =
    Robolectric.buildActivity(LoginActivity::class.java, startIntent).create()

fun createRoboMatchingActivity(startIntent: Intent = Intent()): ActivityController<MatchingActivity> =
    Robolectric.buildActivity(MatchingActivity::class.java, startIntent).create()

fun createRoboAboutActivity(): ActivityController<AboutActivity> =
    Robolectric.buildActivity(AboutActivity::class.java).create()
