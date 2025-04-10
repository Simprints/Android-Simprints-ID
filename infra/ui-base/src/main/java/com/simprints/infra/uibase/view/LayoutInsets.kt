package com.simprints.infra.uibase.view

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.updatePadding
import com.google.android.material.appbar.AppBarLayout
import java.util.LinkedList


/**
 * Automatically applies system bar inset paddings to the provided view.
 *
 * This function should be called on every fragment root view that is displayed to the user and has meaningful UI elements.
 * Fragments that are only responsible for handling the internal navigation graph should not call this function to avoid double padding.
 *
 * Top padding is applied to either
 *   - the first instance of [AppBarLayout] if present
 *   - to the root view.
 */
fun applySystemBarInsets(view: View) {
    ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

        val appBar = findAppBarLayout(LinkedList<View>().apply { add(v) })
        if (appBar != null) {
            appBar.updatePadding(top = bars.top)
            v.updatePadding(bottom = bars.bottom)
        } else {
            v.updatePadding(top = bars.top, bottom = bars.bottom)
        }
        WindowInsetsCompat.CONSUMED
    }
}

/**
 * Recursively traverse the layout and find the first instance of AppBarLayout.
 */
private tailrec fun findAppBarLayout(views: LinkedList<View>): AppBarLayout? {
    if (views.isEmpty()) return null
    val currentView = views.removeFirst()
    if (currentView is AppBarLayout) return currentView
    if (currentView is ViewGroup) {
        for (child in currentView.children) {
            views.add(child)
        }
    }
    return findAppBarLayout(views)
}
