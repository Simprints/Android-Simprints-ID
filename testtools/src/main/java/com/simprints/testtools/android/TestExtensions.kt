package com.simprints.testtools.android

import androidx.test.espresso.ViewInteraction
import com.simprints.testtools.android.matchers.ToastMatcher

fun ViewInteraction.inToast(): ViewInteraction = this.inRoot(ToastMatcher())
