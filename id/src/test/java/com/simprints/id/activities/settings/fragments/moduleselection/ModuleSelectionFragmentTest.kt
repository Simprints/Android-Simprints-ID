package com.simprints.id.activities.settings.fragments.moduleselection

import androidx.fragment.app.testing.launchFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.id.R
import com.simprints.id.activities.BaseActivityTest
import com.simprints.id.testtools.TestApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class ModuleSelectionFragmentTest : BaseActivityTest() {

    @Before
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun test() {
        launchFragment<ModuleSelectionFragment>(themeResId = R.style.AppTheme)
        Truth.assertThat("as").isEqualTo("as")
    }
}
