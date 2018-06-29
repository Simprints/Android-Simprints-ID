package com.simprints.id.testUtils.extensions

import android.app.Activity
import org.robolectric.android.controller.ActivityController

fun <T : Activity> ActivityController<T>.showOnScreen(): ActivityController<T> = this.start().resume().visible()
