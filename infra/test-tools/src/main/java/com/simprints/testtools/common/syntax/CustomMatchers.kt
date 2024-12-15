package com.simprints.testtools.common.syntax

import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

fun hasAnyCompoundDrawable() = object : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description) {
        description.appendText("TextView contains any compound drawable")
    }

    override fun matchesSafely(tv: View?) = tv
        .let { it as? TextView }
        ?.compoundDrawables
        ?.any { it != null }
        ?: false
}

fun hasBackgroundColor(
    @ColorRes targetId: Int,
) = object : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description) {
        description.appendText("View background color ID should be $targetId")
    }

    override fun matchesSafely(v: View?) = v
        ?.let { it.background as? ColorDrawable }
        ?.let { it.color == ResourcesCompat.getColor(v.resources, targetId, null) }
        ?: false
}
