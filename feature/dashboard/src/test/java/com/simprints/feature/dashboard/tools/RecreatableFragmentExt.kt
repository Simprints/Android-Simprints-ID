package com.simprints.feature.dashboard.tools

import android.content.ComponentName
import android.content.Intent
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.simprints.testtools.hilt.HiltTestActivity
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowDialog
import com.google.android.material.R as MR

@PublishedApi
internal const val RECREATABLE_FRAGMENT_TAG = "recreatable"

/**
 * Hosts the fragment in a HiltTestActivity that can be re-created (`scenario.recreate()`), as on rotation.
 * launchFragmentInHiltContainer cannot do this: its fragment is not restored by tag.
 */
internal fun launchRecreatableInHiltContainer(createFragment: () -> Fragment): ActivityScenario<HiltTestActivity> {
    val intent = Intent
        .makeMainActivity(ComponentName(ApplicationProvider.getApplicationContext(), HiltTestActivity::class.java))
        .putExtra(
            "androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY",
            MR.style.Theme_MaterialComponents,
        )
    val scenario = ActivityScenario.launch<HiltTestActivity>(intent)
    scenario.onActivity {
        it.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, createFragment(), RECREATABLE_FRAGMENT_TAG)
            .commitNow()
    }
    shadowOf(Looper.getMainLooper()).idle()
    return scenario
}

internal inline fun <reified T : Fragment> ActivityScenario<HiltTestActivity>.hostedFragment(): T {
    lateinit var fragment: T
    onActivity { fragment = it.supportFragmentManager.findFragmentByTag(RECREATABLE_FRAGMENT_TAG) as T }
    return fragment
}

/** Alert dialogs that are currently on screen. */
internal fun showingAlertDialogs(): List<AlertDialog> = ShadowDialog
    .getShownDialogs()
    .filterIsInstance<AlertDialog>()
    .filter { it.isShowing }
