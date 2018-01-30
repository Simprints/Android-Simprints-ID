package com.simprints.id.tools.roboletric

import com.simprints.id.activities.front.FrontActivity
import com.simprints.id.activities.requestProjectCredentials.RequestProjectCredentialsActivity
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

inline fun createRoboFrontViewActivity() : ActivityController<FrontActivity> {
    return Robolectric.buildActivity(FrontActivity::class.java).create()
}

inline fun createRoboRequestProjectCredentialsActivity() : ActivityController<RequestProjectCredentialsActivity> {
    return Robolectric.buildActivity(RequestProjectCredentialsActivity::class.java).create()
}
