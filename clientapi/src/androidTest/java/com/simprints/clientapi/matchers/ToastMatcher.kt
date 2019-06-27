package com.simprints.clientapi.matchers

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

    private fun isApplicationOverlay(itemType: Int): Boolean {
        return itemType == WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    }

    private fun isToast(item: Root): Boolean {
        val windowToken = item.decorView.windowToken
        val appToken = item.decorView.applicationWindowToken
        return windowToken == appToken
    }

}
