package com.simprints.id

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.Espresso.pressBack
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.DrawerActions
import android.support.test.espresso.contrib.NavigationViewActions.navigateTo
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import com.simprints.id.activities.LaunchActivity
import com.simprints.id.tools.*
import com.simprints.id.tools.StringUtils.getResourceString
import com.simprints.libsimprints.*
import com.simprints.remoteadminclient.ApiException
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.containsString
import org.junit.Assert


fun testHappyWorkflowEnrolment(calloutCredentials: CalloutCredentials,
                               enrolTestRule: ActivityTestRule<LaunchActivity>): String {
    log("testHappyWorkflowEnrolment")
    testLaunchActivityEnrol(calloutCredentials, enrolTestRule)
    testFullHappyWorkflow()
    val guid = testEnrolmentReturnedResult(enrolTestRule)
    testEnrolmentReceivedOnline(calloutCredentials.apiKey, guid)
    return guid
}

fun testHappyWorkflowIdentification(calloutCredentials: CalloutCredentials,
                                    identifyTestRule: ActivityTestRule<LaunchActivity>,
                                    guidToLookFor: String) {
    log("testHappyWorkflowIdentification")
    testLaunchActivityIdentify(calloutCredentials, identifyTestRule)
    testFullHappyWorkflow()
    testMatchingActivityCheckFinished(identifyTestRule)
    testGuidIsTheOnlyReturnedIdentification(identifyTestRule, guidToLookFor)
}

fun testHappyWorkflowVerification(calloutCredentials: CalloutCredentials,
                                  verifyTestRule: ActivityTestRule<LaunchActivity>,
                                  verifyGuid: String) {
    log("testHappyWorkflowVerification")
    testLaunchActivityVerify(calloutCredentials, verifyTestRule, verifyGuid)
    testFullHappyWorkflow()
    testVerificationSuccessful(verifyTestRule, verifyGuid)
}

private fun testLaunchActivityEnrol(calloutCredentials: CalloutCredentials,
                                    enrolTestRule: ActivityTestRule<LaunchActivity>) {
    log("testLaunchActivityEnrol")
    ActivityUtils.launchActivityAndRunOnUiThread(calloutCredentials,
            Constants.SIMPRINTS_REGISTER_INTENT, enrolTestRule)
}

private fun testLaunchActivityIdentify(calloutCredentials: CalloutCredentials,
                                       identifyTestRule: ActivityTestRule<LaunchActivity>) {
    log("testLaunchActivityIdentify")
    ActivityUtils.launchActivityAndRunOnUiThread(calloutCredentials,
            Constants.SIMPRINTS_IDENTIFY_INTENT, identifyTestRule)
}

private fun testLaunchActivityVerify(calloutCredentials: CalloutCredentials,
                                     verifyTestRule: ActivityTestRule<LaunchActivity>,
                                     guid: String?) {
    log("testLaunchActivityVerify")
    ActivityUtils.launchActivityAndRunOnUiThread(calloutCredentials,
            Constants.SIMPRINTS_VERIFY_INTENT, verifyTestRule,
            verifyGuidExtra = guid)
}

private fun testFullHappyWorkflow() {
    log("testFullHappyWorkflow")
    testSetupActivityAndContinue()
    testMainActivityPressScan()
    testMainActivityPressScan()
    testMainActivityCheckGoodScan()
    testMainActivityPressContinue()
}

private fun testSetupActivityAndContinue() {
    log("testSetupActivityAndContinue")
    testSetupActivity()
    testSetupActivityContinue()
}

private fun testSetupActivity() {
    log("testSetupActivity")
    WaitingUtils.tryOnUiUntilTimeout(1000, 50, {
        onView(withId(R.id.tv_consent_text))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.short_consent)))
    })
}

private fun testSetupActivityContinue() {
    log("testSetupActivityContinue")
    WaitingUtils.tryOnUiUntilTimeout(12000, 1000, {
        onView(withId(R.id.confirm_consent_text_view))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.confirm_consent)))
                .perform(click())
    })
}

private fun testMainActivityPressScan() {
    log("testMainActivityScanFinger")
    WaitingUtils.tryOnUiUntilTimeout(10000, 200, {
        onView(withId(R.id.scan_button))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.scan)))
                .perform(click())
    })
}

private fun testMainActivityCheckGoodScan() {
    log("testMainActivityCheckGoodScan")
    WaitingUtils.tryOnUiUntilTimeout(10000, 200, {
        onView(withId(R.id.scan_button))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.good_scan_message)))
    })
}

private fun testMainActivityPressContinue() {
    log("testMainActivityPressContinue")
    WaitingUtils.tryOnUiUntilTimeout(1000, 50, {
        onView(withId(R.id.action_forward))
                .check(matches(isDisplayed()))
                .perform(click())
    })
}

private fun testEnrolmentReturnedResult(enrolTestRule: ActivityTestRule<LaunchActivity>): String {
    log("testEnrolmentReturnedResult")
    val registration = enrolTestRule.activityResult
            .resultData.getParcelableExtra<Registration>(Constants.SIMPRINTS_REGISTRATION)
    val guid = registration.guid
    Assert.assertNotNull(guid)
    return guid
}

private fun testEnrolmentReceivedOnline(apiKey: String, guid: String) {
    log("testEnrolmentReceivedOnline")
    WaitingUtils.tryOnSystemUntilTimeout(12000, 3000, {
        val apiInstance = RemoteAdminUtils.configuredApiInstance
        try {
            // Check to see if the patient made it to the database
            val patientsJson = RemoteAdminUtils.getPatientsNode(apiInstance, apiKey)
            Assert.assertNotNull(patientsJson)
            Assert.assertEquals(1, patientsJson.size().toLong())
            Assert.assertTrue(patientsJson.has(guid))
        } catch (e: ApiException) {
            Assert.assertNull("ApiException", e)
        }
    })
}

private fun testMatchingActivityCheckFinished(identifyTestRule: ActivityTestRule<LaunchActivity>) {
    log("testMatchingActivityCheckFinished")
    WaitingUtils.tryOnSystemUntilTimeout(20000,500, {
        Assert.assertTrue(identifyTestRule.activity.isDestroyed)
    })
}

private fun testGuidIsTheOnlyReturnedIdentification(identifyTestRule: ActivityTestRule<LaunchActivity>, guid: String) {
    log("testGuidIsTheOnlyReturnedIdentification")
    val identifications = identifyTestRule.activityResult
            .resultData.getParcelableArrayListExtra<Identification>(Constants.SIMPRINTS_IDENTIFICATIONS)
    Assert.assertEquals(1, identifications.size.toLong())
    Assert.assertEquals(guid, identifications[0].guid)
    Assert.assertTrue(identifications[0].confidence > 0)
    Assert.assertNotEquals(Tier.TIER_5, identifications[0].tier)
}

private fun testVerificationSuccessful(verifyTestRule: ActivityTestRule<LaunchActivity>, guid: String) {
    log("testVerificationSuccessful")
    val verification = verifyTestRule.activityResult
            .resultData.getParcelableExtra<Verification>(Constants.SIMPRINTS_VERIFICATION)
    Assert.assertEquals(guid, verification.guid)
    Assert.assertTrue(verification.confidence > 0)
    Assert.assertNotEquals(Tier.TIER_5, verification.tier)
}

fun testHappySync(calloutCredentials: CalloutCredentials, identifyTestRule: ActivityTestRule<LaunchActivity>) {
    log("testHappySync")
    testLaunchActivityIdentify(calloutCredentials, identifyTestRule)
    testSetupActivityAndContinue()
    testMainActivitySync()
    testExitFromMainActivity()
}

private fun testMainActivitySync() {
    log("testMainActivitySync")
    testMainActivityOpenDrawer()
    testMainActivityPressSync()
    testVerifyUiForSyncStarted()
    testVerifyUiForSyncCompleted()
    testMainActivityCloseDrawer()
}

private fun testMainActivityOpenDrawer() {
    log("testMainActivityOpenDrawer")
    WaitingUtils.tryOnUiUntilTimeout(4000, 50, {
        onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.open())
    })
}

private fun testMainActivityCloseDrawer() {
    log("testMainActivityCloseDrawer")
    testPressBackButton()
}

private fun testMainActivityPressSync() {
    log("testMainActivityPressSync")
    WaitingUtils.tryOnUiUntilTimeout(1000, 50, {
        onView(withId(R.id.nav_view))
                .perform(navigateTo(R.id.nav_sync))
    })
}

private fun testVerifyUiForSyncStarted() {
    log("testVerifyUiForSyncStarted")
    WaitingUtils.tryOnUiUntilTimeout(1000, 50, {
        onView(anyOf(withText(containsString(getResourceString(R.string.syncing))), withText(containsString(getResourceString(R.string.nav_sync_complete)))))
                .check(matches(isDisplayed()))
    })
}

private fun testVerifyUiForSyncCompleted() {
    log("testVerifyUiForSyncCompleted")
    WaitingUtils.tryOnUiUntilTimeout(SyncParameters.MEDIUM_DATABASE_SYNC_TIMEOUT_MILLIS, 1000, {
        onView(withText(containsString(getResourceString(R.string.nav_sync_complete))))
                .check(matches(isDisplayed()))
    })
}

private fun testExitFromMainActivity() {
    log("testExitFromMainActivity")
    testPressBackButton()
}

private fun testPressBackButton() {
    log("testPressBackButton")
    pressBack()
}
