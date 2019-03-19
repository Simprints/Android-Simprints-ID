package com.simprints.id.activities.settings.fragments.settingsAbout

import android.content.DialogInterface.BUTTON_POSITIVE
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.activities.settings.SettingsAboutActivity
import com.simprints.id.commontesttools.di.DependencyRule
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.syntax.verifyOnce
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.spy
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@Suppress("UsePropertyAccessSyntax")
@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SettingsAboutFragmentTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private lateinit var settingsAboutActivity: SettingsAboutActivity

    private val module by lazy {
        TestAppModule(app,
            localDbManagerRule = DependencyRule.MockRule)
    }

    @Before
    fun setUp() {
        UnitTestConfig(this, module).fullSetup()

        settingsAboutActivity = Robolectric.buildActivity(SettingsAboutActivity::class.java)
           .create()
           .start()
           .resume()
           .get()
    }

    @Test
    fun logoutDialogShown_userClicksOk_presenterShouldPerformLogout() {
        val fragmentManager = settingsAboutActivity.fragmentManager
        val fragment = fragmentManager.findFragmentById(com.simprints.id.R.id.prefContent) as SettingsAboutFragment
        fragment.viewPresenter = spy(fragment.viewPresenter)
        whenever(fragment.viewPresenter) { logout() } thenDoNothing {}
        val dialog = fragment.buildConfirmationDialogForLogout()

        dialog.show()
        dialog.getButton(BUTTON_POSITIVE).performClick()

        verifyOnce(fragment.viewPresenter) { logout() }
    }
}
