package com.simprints.id.activities

import com.google.firebase.FirebaseApp
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppModuleForTests
import com.simprints.id.di.DaggerForTests
import com.simprints.id.domain.consent.GeneralConsent
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.shared.DependencyRule.MockRule
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.testUtils.roboletric.createRoboLaunchActivity
import com.simprints.id.tools.delegates.lazyVar
import junit.framework.Assert
import kotlinx.android.synthetic.main.activity_launch.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class LaunchActivityTest : RxJavaTest, DaggerForTests() {

    @Inject lateinit var preferencesManager: PreferencesManager

    override var module by lazyVar {
        AppModuleForTests(app,
            localDbManagerRule = MockRule(),
            remoteDbManagerRule = MockRule(),
            dbManagerRule = MockRule(),
            scheduledSyncManagerRule = MockRule())
    }

    @Before
    override fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as TestApplication)
        super.setUp()
        testAppComponent.inject(this)
    }

    @Test
    fun enrollmentCallout_showsCorrectGeneralConsentText() {
        val calloutAction = CalloutAction.REGISTER
        preferencesManager.calloutAction = calloutAction

        val controller = createRoboLaunchActivity().start().resume().visible()
        val activity = controller.get()

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val defaultGeneralConsentText = GeneralConsent().assembleText(activity, calloutAction, preferencesManager.programName, preferencesManager.organizationName)
        Assert.assertEquals(defaultGeneralConsentText, generalConsentText)
    }

    @Test
    fun identifyCallout_showsCorrectGeneralConsentText() {
        val calloutAction = CalloutAction.IDENTIFY
        preferencesManager.calloutAction = calloutAction

        val controller = createRoboLaunchActivity().start().resume().visible()
        val activity = controller.get()

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val defaultGeneralConsentText = GeneralConsent().assembleText(activity, calloutAction, preferencesManager.programName, preferencesManager.organizationName)
        Assert.assertEquals(defaultGeneralConsentText, generalConsentText)
    }
}
