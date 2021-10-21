package com.simprints.id.activities.settings.fragments.moduleselection

import androidx.core.content.res.ResourcesCompat
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.R
import com.simprints.id.activities.BaseActivityTest
import com.simprints.id.activities.settings.ModuleSelectionActivity
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testtools.unit.robolectric.createAndStartActivity
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(
    instrumentedPackages = ["androidx.loader.content"],
    application = TestApplication::class,
    shadows = [ShadowAndroidXMultiDex::class]
)
class ModuleSelectionFragmentTest : BaseActivityTest() {

    @Before
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun onLaunchModuleSelectionFragmentConfirmSearchViewIsVisible() {
        createAndStartActivity<ModuleSelectionActivity>()
        verifyConfirmationUIVisibility(R.id.searchView, ViewMatchers.Visibility.VISIBLE)
        verifyConfirmationUIVisibility(androidx.appcompat.R.id.search_src_text, ViewMatchers.Visibility.VISIBLE)
    }

    @Test
    fun onlaunchFragmentAssertGetFontIsCalled() {
        mockkStatic(ResourcesCompat::class)
        createAndStartActivity<ModuleSelectionActivity>()
        verify { ResourcesCompat.getFont(any(), any()) }
    }

    @Test(expected = Test.None::class)
    fun onlaunchFragmentThrowGetFontExceptionContinuesExecution() {
        mockkStatic(ResourcesCompat::class)
        createAndStartActivity<ModuleSelectionActivity>()
        every { ResourcesCompat.getFont(any(), any()) } throws Exception()
    }

    private fun verifyConfirmationUIVisibility(id: Int, expectedVisibility: ViewMatchers.Visibility) {
        Espresso.onView(ViewMatchers.withId(id)).check(
            ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(expectedVisibility))
        )
    }
}
