package com.simprints.id

import android.support.test.espresso.Espresso
import android.support.test.espresso.NoMatchingViewException
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.DrawerActions
import android.support.test.espresso.contrib.NavigationViewActions.navigateTo
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import com.simprints.id.activities.LaunchActivity
import com.simprints.id.tools.*
import com.simprints.libsimprints.*
import com.simprints.remoteadminclient.ApiException
import org.hamcrest.Matchers.anyOf
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
    testMainActivityScanFinger()
    testMainActivityScanFinger()
    testMainActivityPressContinue()
}

private fun testSetupActivityAndContinue() {
    log("testSetupActivityAndContinue")
    testSetupActivity()
    testSetupActivityContinue()
}

private fun testSetupActivity() {
    log("testSetupActivity")
    Espresso.onView(withId(R.id.tv_consent_text))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.short_consent)))

    WaitingUtils.waitOnUiForSetupToFinish()
}

private fun testSetupActivityContinue() {
    log("testSetupActivityContinue")
    Espresso.onView(withId(R.id.confirm_consent_text_view))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.confirm_consent)))
            .perform(click())

    WaitingUtils.waitOnUiForActivityToSettle()
}

private fun testMainActivityScanFinger() {
    log("testMainActivityScanFinger")
    Espresso.onView(withId(R.id.scan_button))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.scan)))
            .perform(click())

    WaitingUtils.waitOnUiForScanningToComplete()
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
    WaitingUtils.waitOnSystemForQuickDataCalls()

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

    WaitingUtils.waitOnSystemToSettle()
}

private fun testGuidIsTheOnlyReturnedIdentification(identifyTestRule: ActivityTestRule<LaunchActivity>, guid: String) {
    log("testGuidIsTheOnlyReturnedIdentification")
    WaitingUtils.waitOnUiForMatchingToComplete()

    val identifications = identifyTestRule.activityResult
            .resultData.getParcelableArrayListExtra<Identification>(Constants.SIMPRINTS_IDENTIFICATIONS)
    Assert.assertEquals(1, identifications.size.toLong())
    Assert.assertEquals(guid, identifications[0].guid)
    Assert.assertTrue(identifications[0].confidence > 0)
    Assert.assertNotEquals(Tier.TIER_5, identifications[0].tier)

    WaitingUtils.waitOnSystemToSettle()
}

private fun testVerificationSuccessful(verifyTestRule: ActivityTestRule<LaunchActivity>, guid: String) {
    log("testVerificationSuccessful")
    val verification = verifyTestRule.activityResult
            .resultData.getParcelableExtra<Verification>(Constants.SIMPRINTS_VERIFICATION)
    Assert.assertEquals(guid, verification.guid)
    Assert.assertTrue(verification.confidence > 0)
    Assert.assertNotEquals(Tier.TIER_5, verification.tier)

    WaitingUtils.waitOnSystemToSettle()
}

private fun testMainActivityPressContinue() {
    log("testMainActivityPressContinue")
    Espresso.onView(withId(R.id.action_forward))
            .check(matches(isDisplayed()))
            .perform(click())
}

fun testHappySync(calloutCredentials: CalloutCredentials, identifyTestRule: ActivityTestRule<LaunchActivity>) {
    log("testHappySync")
    testLaunchActivityIdentify(calloutCredentials, identifyTestRule)
    testSetupActivityAndContinue()
    testMainActivitySync()
}

private fun testMainActivitySync() {
    log("testMainActivitySync")
    testMainActivityOpenDrawer()
    testMainActivityPressSync()
    testWaitAndVerifyUiForSyncCompleted()
}

private fun testMainActivityOpenDrawer() {
    log("testMainActivityOpenDrawer")
    Espresso.onView(withId(R.id.drawer_layout))
            .perform(DrawerActions.open())
    WaitingUtils.waitOnUiForActivityToSettle()
}

private fun testMainActivityPressSync() {
    log("testMainActivityPressSync")
    Espresso.onView(withId(R.id.nav_view))
            .perform(navigateTo(R.id.nav_sync))
    testVerifyUiForSyncStarted()

}

private fun testVerifyUiForSyncStarted() {
    log("testVerifyUiForSyncStarted")
    WaitingUtils.waitOnUiForSyncingToStart()
    Espresso.onView(anyOf(withText(R.string.syncing), withText(R.string.nav_sync_complete)))
            .check(matches(isDisplayed()))
}

private fun testWaitAndVerifyUiForSyncCompleted() {
    log("testWaitAndVerifyUiForSyncCompleted")
    testCheckUiForSyncCompleted()
}

private fun testCheckUiForSyncCompleted(iteration: Int = 1) {
    WaitingUtils.waitOnUiForMediumSyncInterval()
    log("testCheckUiForSyncCompleted seconds elapsed: " + iteration * SyncParameters.SYNC_CHECK_INTERVAL)
    try {
        Espresso.onView(withText(R.string.nav_sync_complete))
                .check(matches(isDisplayed()))
    } catch (e: NoMatchingViewException) {
        try {
            Espresso.onView(withText(R.string.syncing))
                    .check(matches(isDisplayed()))
            testCheckUiForSyncCompleted(iteration + 1)
        } catch (e: NoMatchingViewException) {
            log("testCheckUiForSyncCompleted failed: not showing syncing progress or complete")
            throw Exception("Sync failed to complete")
        }
    }
}