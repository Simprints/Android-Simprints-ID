package com.simprints.id.activities.settings.fragments.settingsPreference

import android.content.DialogInterface.BUTTON_POSITIVE
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.activities.settings.SettingsAboutActivity
import com.simprints.id.activities.settings.fragments.settingsAbout.SettingsAboutFragment
import com.simprints.id.di.AppModuleForTests
import com.simprints.id.di.DaggerForTests
import com.simprints.id.shared.DependencyRule
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.delegates.lazyVar
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@Suppress("UsePropertyAccessSyntax")
@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SettingsAboutFragmentTest: RxJavaTest, DaggerForTests() {

    lateinit var settingsAboutActivity: SettingsAboutActivity

    override var module by lazyVar {
        AppModuleForTests(app,
            localDbManagerRule = DependencyRule.MockRule)
    }

    @Before
    override fun setUp() {
        app = (ApplicationProvider.getApplicationContext() as TestApplication)
        FirebaseApp.initializeApp(app)
        super.setUp()
        testAppComponent.inject(this)

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
        doNothing().whenever(fragment.viewPresenter).logout()
        val dialog = fragment.buildConfirmationDialogForLogout()

        dialog.show()
        dialog.getButton(BUTTON_POSITIVE).performClick()

        verify(fragment.viewPresenter, times(1)).logout()
    }
}
