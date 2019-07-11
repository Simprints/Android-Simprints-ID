package com.simprints.testtools.android.matchers

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.view.WindowManager
import androidx.test.espresso.Root
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class ToastMatcher : TypeSafeMatcher<Root>() {

    override fun describeTo(description: Description?) {
        description?.appendText("is toast")
    }

    override fun matchesSafely(item: Root): Boolean {
        val itemType = item.windowLayoutParams.get().type
        return if (isApplicationOverlay(itemType))
            isToast(item)
        else
            false
    }

    @Suppress("DEPRECATION")
    private fun isApplicationOverlay(itemType: Int): Boolean {
        return itemType == if (SDK_INT >= O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_TOAST
        }
    }

    private fun isToast(item: Root): Boolean {
        val windowToken = item.decorView.windowToken
        val appToken = item.decorView.applicationWindowToken
        return windowToken == appToken
    }

}
