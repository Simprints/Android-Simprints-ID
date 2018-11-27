package com.simprints.id.activities.alert

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.FirebaseApp
import com.simprints.id.activities.IntentKeys
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.data.analytics.eventData.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.di.AppModuleForTests
import com.simprints.id.di.DaggerForTests
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.shared.DependencyRule.MockRule
import com.simprints.id.testUtils.extensions.showOnScreen
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.testUtils.roboletric.createRoboAlertActivity
import com.simprints.id.testUtils.roboletric.setupSessionEventsManagerToAvoidRealmCall
import com.simprints.id.tools.delegates.lazyVar
import junit.framework.Assert
import kotlinx.android.synthetic.main.activity_alert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class AlertActivityTest : DaggerForTests() {

    @Inject lateinit var sessionEventsLocalDbManager: SessionEventsLocalDbManager

    override var module by lazyVar {
        AppModuleForTests(app,
            remoteDbManagerRule = MockRule,
            localDbManagerRule = MockRule,
            analyticsManagerRule = MockRule,
            secureDataManagerRule = MockRule,
            sessionEventsLocalDbManagerRule = MockRule)
    }

    @Before
    override fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as TestApplication)
        super.setUp()
        testAppComponent.inject(this)

        setupSessionEventsManagerToAvoidRealmCall(sessionEventsLocalDbManager)
    }

    @Test
    fun anUnexpectedErrorOccurs_shouldShowTheRightAlertView() {
        val alertType = ALERT_TYPE.UNEXPECTED_ERROR
        val controller = createRoboAlertActivity(createIntentForAlertType(alertType)).showOnScreen()
        val activity = controller.get() as AlertActivity
        controller.visible()

        checkAlertIsShownCorrectly(activity, alertType)
    }

    @Test
    fun anBluetoothErrorOccurs_shouldShowTheRightAlertView() {
        val alertType = ALERT_TYPE.BLUETOOTH_NOT_ENABLED
        val controller = createRoboAlertActivity(createIntentForAlertType(alertType)).showOnScreen()
        val activity = controller.get() as AlertActivity
        controller.visible()
        activity.right_button.performClick()

        val intent = Shadows.shadowOf(activity).nextStartedActivity
        Assert.assertEquals(intent.action, "android.settings.BLUETOOTH_SETTINGS")

        checkAlertIsShownCorrectly(activity, alertType)
    }

    @Test
    fun anOfflineError_shouldShowTheRightAlertView() {
        val alertType = ALERT_TYPE.GUID_NOT_FOUND_OFFLINE
        val controller = createRoboAlertActivity(createIntentForAlertType(alertType)).showOnScreen()
        val activity = controller.get() as AlertActivity
        controller.visible()
        activity.right_button.performClick()

        val intent = Shadows.shadowOf(activity).nextStartedActivity
        Assert.assertEquals(intent.action, "android.settings.WIFI_SETTINGS")

        checkAlertIsShownCorrectly(activity, alertType)
    }

    private fun createIntentForAlertType(alertType: ALERT_TYPE) = Intent().apply {
        putExtra(IntentKeys.alertActivityAlertTypeKey, alertType)
    }

    private fun checkAlertIsShownCorrectly(alertActivity: AlertActivity, alertType: ALERT_TYPE) {
        Assert.assertEquals(getBackgroundColor(alertActivity.alertLayout), getColorWithColorRes(alertType.backgroundColor))

        if (alertType.isTwoButton) Assert.assertEquals(getBackgroundColor(alertActivity.left_button), getColorWithColorRes(alertType.backgroundColor))
        Assert.assertEquals(getBackgroundColor(alertActivity.right_button), getColorWithColorRes(alertType.backgroundColor))

        Assert.assertEquals(alertActivity.alert_title.text, alertActivity.resources.getString(alertType.alertTitleId))

        val alertImageDrawableShown = Shadows.shadowOf(alertActivity.alert_image.drawable).createdFromResId
        Assert.assertEquals(alertImageDrawableShown, alertType.alertMainDrawableId)

        Assert.assertEquals(alertActivity.message.text, alertActivity.resources.getString(alertType.alertMessageId))

        if (alertType.isTwoButton) Assert.assertEquals(alertActivity.left_button.text, alertActivity.resources.getString(alertType.alertLeftButtonTextId))
        Assert.assertEquals(alertActivity.right_button.text, alertActivity.resources.getString(alertType.alertRightButtonTextId))
    }

    private fun getBackgroundColor(view: View): Int =
        if (view.background is ColorDrawable) {
            (view.background as ColorDrawable).color
        } else {
            Color.TRANSPARENT
        }

    private fun getColorWithColorRes(colorRes: Int, resources: Resources = app.resources) = ResourcesCompat.getColor(resources, colorRes, null)
}
