package com.simprints.id.activities.settings.fragments.moduleselection

import androidx.core.content.res.ResourcesCompat
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.R
import com.simprints.id.activities.settings.ModuleSelectionActivity
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testtools.unit.robolectric.createAndStartActivity
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(
    instrumentedPackages = ["androidx.loader.content"],
    shadows = [ShadowAndroidXMultiDex::class]
)
class ModuleSelectionFragmentTest {

    @Test
    fun onLaunchModuleSelectionFragmentConfirmSearchViewIsVisible() {
        createAndStartActivity<ModuleSelectionActivity>()
        verifyConfirmationUIVisibility(R.id.searchView, ViewMatchers.Visibility.VISIBLE)
        verifyConfirmationUIVisibility(
            androidx.appcompat.R.id.search_src_text,
            ViewMatchers.Visibility.VISIBLE
        )
    }

    @Test
    fun onLaunchModuleSelectionFragmentSearchDoneRecyclerViewGetsFocus() {
        createAndStartActivity<ModuleSelectionActivity>()
        onView(withId(androidx.appcompat.R.id.search_src_text))
            .perform(typeText("any text"))
            .perform(pressImeActionButton())
        onView(withId(R.id.rvModules)).check(
            matches(ViewMatchers.hasFocus())
        )
    }

    @Test
    fun onLaunchFragmentAssertGetFontIsCalled() {
        mockkStatic(ResourcesCompat::class)
        createAndStartActivity<ModuleSelectionActivity>()
        verify { ResourcesCompat.getFont(any(), any()) }
    }

    @Test(expected = Test.None::class)
    fun onLaunchFragmentThrowGetFontExceptionContinuesExecution() {
        mockkStatic(ResourcesCompat::class)
        createAndStartActivity<ModuleSelectionActivity>()
        every { ResourcesCompat.getFont(any(), any()) } throws Exception()
    }

    private fun verifyConfirmationUIVisibility(
        id: Int,
        expectedVisibility: ViewMatchers.Visibility
    ) {
        onView(withId(id)).check(
            matches(ViewMatchers.withEffectiveVisibility(expectedVisibility))
        )
    }
}
