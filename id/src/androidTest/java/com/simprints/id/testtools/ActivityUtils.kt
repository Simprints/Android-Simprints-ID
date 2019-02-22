package com.simprints.id.testtools

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import com.schibsted.spain.barista.interaction.PermissionGranter
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.activities.collectFingerprints.CollectFingerprintsActivity
import com.simprints.id.commontesttools.models.TestCalloutCredentials
import com.simprints.libsimprints.Constants
import com.simprints.testframework.android.log
import com.simprints.testframework.android.runActivityOnUiThread
import java.util.*

fun checkLoginFromIntentActivityTestRule() =
    ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

private val permissions = ArrayList(Arrays.asList(
    Manifest.permission.ACCESS_NETWORK_STATE,
    Manifest.permission.BLUETOOTH,
    Manifest.permission.INTERNET,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.RECEIVE_BOOT_COMPLETED,
    Manifest.permission.WAKE_LOCK,
    Manifest.permission.VIBRATE
))

fun launchActivityAndRunOnUiThread(testCalloutCredentials: TestCalloutCredentials,
                                   action: String,
                                   activityTestRule: ActivityTestRule<*>,
                                   verifyGuidExtra: String? = null) {
    val intent = createIntent(testCalloutCredentials, action)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (verifyGuidExtra != null) intent.putExtra(Constants.SIMPRINTS_VERIFY_GUID, verifyGuidExtra)
    activityTestRule.launchActivity(intent)
    runActivityOnUiThread(activityTestRule)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) grantPermissions()
}

private fun createIntent(testCalloutCredentials: TestCalloutCredentials, action: String): Intent {
    val targetContext = getInstrumentation().targetContext
    val intent = Intent(targetContext, CheckLoginFromIntentActivity::class.java)
    intent.action = action
    if (testCalloutCredentials.projectId.isNotEmpty())
        intent.putExtra(Constants.SIMPRINTS_PROJECT_ID, testCalloutCredentials.projectId)
    else intent.putExtra(Constants.SIMPRINTS_API_KEY, testCalloutCredentials.legacyApiKey)
    intent.putExtra(Constants.SIMPRINTS_USER_ID, testCalloutCredentials.userId)
    intent.putExtra(Constants.SIMPRINTS_MODULE_ID, testCalloutCredentials.moduleId)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    return intent
}

fun grantPermissions() {
    // Allow all first-app permissions and dismiss the dialog box
    log("ActivityUtils.grantPermissions(): granting permissions")
    permissions.forEach { PermissionGranter.allowPermissionsIfNeeded(it) }
}

fun launchCollectFingerprintsActivity(collectFingerprintsTestRule: ActivityTestRule<CollectFingerprintsActivity>) {
    val intent = Intent()
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    collectFingerprintsTestRule.launchActivity(intent)
    runActivityOnUiThread(collectFingerprintsTestRule)
}
