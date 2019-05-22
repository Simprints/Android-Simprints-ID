package com.simprints.id.activities.alert

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.testtools.common.di.DependencyRule.MockRule
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.data.analytics.eventdata.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.domain.alert.AlertActivityViewModel
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.testtools.state.RobolectricTestMocker.setupSessionEventsManagerToAvoidRealmCall
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testtools.unit.robolectric.createActivity
import com.simprints.testtools.unit.robolectric.showOnScreen
import kotlinx.android.synthetic.main.activity_alert.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class AlertActivityTest {

    private val app = ApplicationProvider.getApplicationContext<Context>() as TestApplication

    @Inject lateinit var sessionEventsLocalDbManager: SessionEventsLocalDbManager

    private val module by lazy {
        TestAppModule(app,
            remoteDbManagerRule = MockRule,
            localDbManagerRule = MockRule,
            crashReportManagerRule = MockRule,
            secureDataManagerRule = MockRule,
            sessionEventsLocalDbManagerRule = MockRule)
    }

    @Before
    fun setUp() {
        UnitTestConfig(this, module).fullSetup()

        setupSessionEventsManagerToAvoidRealmCall(sessionEventsLocalDbManager)
    }

    @Test
    fun anUnexpectedErrorOccurs_shouldShowTheRightAlertView() {
        val alertType = AlertActivityViewModel.UNEXPECTED_ERROR
        val controller = createRoboAlertActivity(createIntentForAlertType(alertType)).showOnScreen()
        val activity = controller.get() as AlertActivity
        controller.visible()

        checkAlertIsShownCorrectly(activity, alertType)
    }

    @Test
    fun anBluetoothErrorOccurs_shouldShowTheRightAlertView() {
        val alertType = AlertActivityViewModel.BLUETOOTH_NOT_ENABLED
        val controller = createRoboAlertActivity(createIntentForAlertType(alertType)).showOnScreen()
        val activity = controller.get() as AlertActivity
        controller.visible()
        activity.right_button.performClick()

        val intent = Shadows.shadowOf(activity).nextStartedActivity
        assertEquals(intent.action, "android.settings.BLUETOOTH_SETTINGS")

        checkAlertIsShownCorrectly(activity, alertType)
    }

    @Test
    fun anOfflineError_shouldShowTheRightAlertView() {
        val alertType = AlertActivityViewModel.GUID_NOT_FOUND_OFFLINE
        val controller = createRoboAlertActivity(createIntentForAlertType(alertType)).showOnScreen()
        val activity = controller.get() as AlertActivity
        controller.visible()
        activity.right_button.performClick()

        val intent = Shadows.shadowOf(activity).nextStartedActivity
        assertEquals(intent.action, "android.settings.WIFI_SETTINGS")

        checkAlertIsShownCorrectly(activity, alertType)
    }

    private fun createRoboAlertActivity(intent: Intent) =
        createActivity<AlertActivity>(intent)

    private fun createIntentForAlertType(alertActivity: AlertActivityViewModel) = Intent().apply {
        putExtra(IntentKeys.alertActivityAlertTypeKey, alertActivity)
    }

    private fun checkAlertIsShownCorrectly(alertActivity: AlertActivity, alert: AlertActivityViewModel) {
        assertEquals(getBackgroundColor(alertActivity.alertLayout), getColorWithColorRes(alert.backgroundColor))

        if (alert.isTwoButton()) assertEquals(getBackgroundColor(alertActivity.left_button), getColorWithColorRes(alert.backgroundColor))
        assertEquals(getBackgroundColor(alertActivity.right_button), getColorWithColorRes(alert.backgroundColor))

        assertEquals(alertActivity.alert_title.text, alertActivity.resources.getString(alert.title))

        val alertImageDrawableShown = Shadows.shadowOf(alertActivity.alert_image.drawable).createdFromResId
        assertEquals(alertImageDrawableShown, alert.mainDrawable)

        assertEquals(alertActivity.message.text, alertActivity.resources.getString(alert.message))

        if (alert.isTwoButton()) assertEquals(alertActivity.left_button.text, alertActivity.resources.getString(alert.leftButton.buttonText))
        assertEquals(alertActivity.right_button.text, alertActivity.resources.getString(alert.rightButton.buttonText))
    }

    private fun getBackgroundColor(view: View): Int =
        if (view.background is ColorDrawable) {
            (view.background as ColorDrawable).color
        } else {
            Color.TRANSPARENT
        }

    private fun getColorWithColorRes(colorRes: Int, resources: Resources = app.resources) = ResourcesCompat.getColor(resources, colorRes, null)

    private fun AlertActivityViewModel.isTwoButton() =
        leftButton != AlertActivityViewModel.ButtonAction.None || rightButton != AlertActivityViewModel.ButtonAction.None
}
