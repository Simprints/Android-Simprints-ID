package com.simprints.clientapi.extensions

import androidx.test.espresso.ViewInteraction
import com.simprints.testtools.android.matchers.ToastMatcher

fun ViewInteraction.inToast(): ViewInteraction = this.inRoot(ToastMatcher())
