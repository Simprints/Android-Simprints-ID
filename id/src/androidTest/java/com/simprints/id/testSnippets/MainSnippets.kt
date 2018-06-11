package com.simprints.id.testSnippets

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.Espresso.pressBack
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.DrawerActions
import android.support.test.espresso.contrib.NavigationViewActions.navigateTo
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import com.simprints.id.R
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.testTools.*
import com.simprints.id.testTools.StringUtils.getResourceString
import com.simprints.libsimprints.*
import com.simprints.remoteadminclient.ApiException
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.containsString
import org.junit.Assert.*


fun launchActivityEnrol(calloutCredentials: CalloutCredentials,
                        enrolTestRule: ActivityTestRule<CheckLoginFromIntentActivity>) {
    log("launchActivityEnrol")
    ActivityUtils.launchActivityAndRunOnUiThread(calloutCredentials,
        Constants.SIMPRINTS_REGISTER_INTENT, enrolTestRule)
}

fun launchActivityIdentify(calloutCredentials: CalloutCredentials,
                           identifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>) {
    log("launchActivityIdentify")
    ActivityUtils.launchActivityAndRunOnUiThread(calloutCredentials,
        Constants.SIMPRINTS_IDENTIFY_INTENT, identifyTestRule)
}

fun launchActivityVerify(calloutCredentials: CalloutCredentials,
                         verifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>,
                         guid: String?) {
    log("launchActivityVerify")
    ActivityUtils.launchActivityAndRunOnUiThread(calloutCredentials,
        Constants.SIMPRINTS_VERIFY_INTENT, verifyTestRule,
        verifyGuidExtra = guid)
}

fun fullHappyWorkflow() {
    log("fullHappyWorkflow")
    setupActivityAndContinue()
    mainActivityPressScan()
    mainActivityPressScan()
    mainActivityCheckGoodScan()
    mainActivityPressContinue()
}

private fun setupActivityAndContinue() {
    log("setupActivityAndContinue")
    setupActivity()
    setupActivityContinue()
}

private fun setupActivity() {
    log("setupActivity")
    WaitingUtils.tryOnUiUntilTimeout(1000, 50, {
        ActivityUtils.grantPermissions()
        onView(withId(R.id.consentTextView))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.short_consent)))
    })
}

private fun setupActivityContinue() {
    log("setupActivityContinue")
    WaitingUtils.tryOnUiUntilTimeout(12000, 500, {
        onView(withId(R.id.confirmConsentTextView))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.confirm_consent)))
            .perform(click())
    })
}

private fun mainActivityPressScan() {
    log("mainActivityPressScan")
    WaitingUtils.tryOnUiUntilTimeout(10000, 200, {
        onView(withId(R.id.scan_button))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.scan)))
            .perform(click())
    })
}

private fun mainActivityCheckGoodScan() {
    log("mainActivityCheckGoodScan")
    WaitingUtils.tryOnUiUntilTimeout(10000, 200, {
        onView(withId(R.id.scan_button))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.good_scan_message)))
    })
}

private fun mainActivityPressContinue() {
    log("mainActivityPressContinue")
    WaitingUtils.tryOnUiUntilTimeout(1000, 50, {
        onView(withId(R.id.action_forward))
            .check(matches(isDisplayed()))
            .perform(click())
    })
}

fun mainActivityEnrolmentCheckFinished(enrolTestRule: ActivityTestRule<CheckLoginFromIntentActivity>) {
    log("mainActivityEnrolmentCheckFinished")
    WaitingUtils.tryOnSystemUntilTimeout(5000, 500, {
        assertTrue(enrolTestRule.activity.isDestroyed)
    })
}

fun enrolmentReturnedResult(enrolTestRule: ActivityTestRule<CheckLoginFromIntentActivity>): String {
    log("enrolmentReturnedResult")
    val registration = enrolTestRule.activityResult
        .resultData.getParcelableExtra<Registration>(Constants.SIMPRINTS_REGISTRATION)
    val guid = registration.guid
    assertNotNull(guid)
    return guid
}

fun enrolmentReceivedOnline(apiKey: String, guid: String) {
    log("enrolmentReceivedOnline")
    WaitingUtils.tryOnSystemUntilTimeout(12000, 3000, {
        val apiInstance = RemoteAdminUtils.configuredApiInstance
        try {
            // Check to see if the patient made it to the database
            val patientsJson = RemoteAdminUtils.getPatientsNode(apiInstance, apiKey)
            assertNotNull(patientsJson)
            assertEquals(1, patientsJson.size().toLong())
            assertTrue(patientsJson.has(guid))
        } catch (e: ApiException) {
            assertNull("ApiException", e)
        }
    })
}

fun matchingActivityIdentificationCheckFinished(identifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>) {
    log("matchingActivityIdentificationCheckFinished")
    WaitingUtils.tryOnSystemUntilTimeout(20000, 500, {
        assertTrue(identifyTestRule.activity.isDestroyed)
    })
}

fun guidIsTheOnlyReturnedIdentification(identifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>, guid: String) {
    log("guidIsTheOnlyReturnedIdentification")
    val identifications = identifyTestRule.activityResult
        .resultData.getParcelableArrayListExtra<Identification>(Constants.SIMPRINTS_IDENTIFICATIONS)
    assertEquals(1, identifications.size.toLong())
    assertEquals(guid, identifications[0].guid)
    assertTrue(identifications[0].confidence > 0)
    assertNotEquals(Tier.TIER_5, identifications[0].tier)
}

fun matchingActivityVerificationCheckFinished(verifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>) {
    log("matchingActivityVerificationCheckFinished")
    WaitingUtils.tryOnSystemUntilTimeout(5000, 500, {
        assertTrue(verifyTestRule.activity.isDestroyed)
    })
}

fun verificationSuccessful(verifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>, guid: String) {
    log("verificationSuccessful")
    val verification = verifyTestRule.activityResult
        .resultData.getParcelableExtra<Verification>(Constants.SIMPRINTS_VERIFICATION)
    assertEquals(guid, verification.guid)
    assertTrue(verification.confidence > 0)
    assertNotEquals(Tier.TIER_5, verification.tier)
}

fun verificationNotAMatch(verifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>, guid: String) {
    log("verificationSuccessful")
    val verification = verifyTestRule.activityResult
        .resultData.getParcelableExtra<Verification>(Constants.SIMPRINTS_VERIFICATION)
    assertEquals(guid, verification.guid)
    assertTrue(verification.confidence > 0)
    assertEquals(Tier.TIER_5, verification.tier)
}

fun happySync(calloutCredentials: CalloutCredentials, identifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>) {
    log("happySync")
    launchActivityIdentify(calloutCredentials, identifyTestRule)
    setupActivityAndContinue()
    mainActivitySync()
    exitFromMainActivity()
}

private fun mainActivitySync() {
    log("mainActivitySync")
    mainActivityOpenDrawer()
    mainActivityPressSync()
    verifyUiForSyncStarted()
    verifyUiForSyncCompleted()
    mainActivityCloseDrawer()
}

private fun mainActivityOpenDrawer() {
    log("mainActivityOpenDrawer")
    WaitingUtils.tryOnUiUntilTimeout(4000, 50, {
        onView(withId(R.id.drawer_layout))
            .perform(DrawerActions.open())
    })
}

private fun mainActivityCloseDrawer() {
    log("mainActivityCloseDrawer")
    pressBackButton()
}

private fun mainActivityPressSync() {
    log("mainActivityPressSync")
    WaitingUtils.tryOnUiUntilTimeout(4000, 50, {
        onView(withId(R.id.nav_view))
            .perform(navigateTo(R.id.nav_sync))
    })
}

private fun verifyUiForSyncStarted() {
    log("verifyUiForSyncStarted")
    WaitingUtils.tryOnUiUntilTimeout(1000, 50, {
        onView(anyOf(withText(containsString(getResourceString(R.string.syncing))), withText(containsString(getResourceString(R.string.nav_sync_complete)))))
            .check(matches(isDisplayed()))
    })
}

private fun verifyUiForSyncCompleted() {
    log("verifyUiForSyncCompleted")
    WaitingUtils.tryOnUiUntilTimeout(SyncParameters.MEDIUM_DATABASE_SYNC_TIMEOUT_MILLIS, 1000, {
        onView(withText(containsString(getResourceString(R.string.nav_sync_complete))))
            .check(matches(isDisplayed()))
    })
}

private fun exitFromMainActivity() {
    log("exitFromMainActivity")
    pressBackButton()
}

private fun pressBackButton() {
    log("pressBackButton")
    pressBack()
}
