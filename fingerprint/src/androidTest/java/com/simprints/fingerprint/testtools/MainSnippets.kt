package com.simprints.fingerprint.testtools

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import com.simprints.fingerprint.R
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.libsimprints.*
import com.simprints.testtools.android.*
import com.simprints.testtools.android.WaitingUtils.UI_POLLING_INTERVAL_LONG
import com.simprints.testtools.android.WaitingUtils.UI_POLLING_INTERVAL_SHORT
import com.simprints.testtools.android.WaitingUtils.UI_TIMEOUT
import org.hamcrest.Matchers.not
import org.junit.Assert.*

fun fullHappyWorkflow(numberOfScans: Int = 2, dialogResult: String = "✓ LEFT THUMB\n✓ LEFT INDEX FINGER\n") {
    log("fullHappyWorkflow")
    setupActivityAndContinue()

    repeat(numberOfScans) { collectFingerprintsPressScan() }

    checkIfDialogIsDisplayedWithResultAndClickConfirm(dialogResult)
}

fun setupActivityAndContinue() {
    log("setupActivityAndContinue")
    setupActivity()
    setupActivityContinue()
}

fun setupActivityAndDecline() {
    log("setupActivityAndDecline")
    setupActivity()
    setupActivityDecline()
}

fun setupActivity() {
    log("setupActivity")
    tryOnUiUntilTimeout(UI_TIMEOUT, UI_POLLING_INTERVAL_SHORT) {
        grantPermissions()
        onView(withId(R.id.generalConsentTextView))
            .check(matches(isDisplayed()))
    }
}

private fun setupActivityContinue() {
    log("setupActivityContinue")
    tryOnUiUntilTimeout(UI_TIMEOUT, UI_POLLING_INTERVAL_LONG) {
        onView(withId(R.id.consentAcceptButton))
            .check(matches(isDisplayed()))
            .perform(click())
    }
}

fun setupActivityDecline() {
    log("setupActivityContinue")
    tryOnUiUntilTimeout(UI_TIMEOUT, UI_POLLING_INTERVAL_LONG) {
        onView(withId(R.id.consentDeclineButton))
            .check(matches(isDisplayed()))
            .perform(click())
    }
}

fun collectFingerprintsPressScan() {
    log("collectFingerprintsPressScan")
    tryOnUiUntilTimeout(UI_TIMEOUT, UI_POLLING_INTERVAL_LONG) {
        onView(withId(R.id.scan_button))
            .check(matches(not(withText(R.string.cancel_button))))
            .perform(click())
    }
    Thread.sleep(500) //Wait for ViewPager animation
}

fun skipFinger() {
    log("skipFinger")
    tryOnUiUntilTimeout(UI_TIMEOUT, UI_POLLING_INTERVAL_LONG) {
        onView(withId(R.id.missingFingerText))
            .check(matches(isDisplayed()))
            .perform(click())
    }
}

fun waitForSplashScreenAppearsAndDisappears() {
    log("checkSplashScreen")
    tryOnUiUntilTimeout(UI_TIMEOUT, UI_POLLING_INTERVAL_LONG) {
        onView(withId(R.id.splashGetReady))
            .check(matches(isDisplayed()))
    }

    tryOnUiUntilTimeout(UI_TIMEOUT, UI_POLLING_INTERVAL_LONG) {
        onView(withId(R.id.splashGetReady))
            .check(doesNotExist())
    }

    waitOnUi(2000)
}

fun checkIfDialogIsDisplayedWithResultAndClickConfirm(dialogResult: String = "✓ LEFT THUMB\n✓ LEFT INDEX FINGER\n") {
    tryOnUiUntilTimeout(UI_TIMEOUT, UI_POLLING_INTERVAL_SHORT) {
        onView(withText(getResourceString(R.string.confirm_fingers_dialog_title)))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(android.R.id.message))
            .inRoot(isDialog())
            .check(matches(withText(dialogResult)))
            .check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())
    }
}

fun collectFingerprintsEnrolmentCheckFinished(enrolTestRule: ActivityTestRule<CheckLoginFromIntentActivity>) {
    log("collectFingerprintsEnrolmentCheckFinished")
    tryOnSystemUntilTimeout(UI_TIMEOUT, UI_POLLING_INTERVAL_LONG) {
        assertTrue(enrolTestRule.activity.isDestroyed)
    }
}

fun enrolmentReturnedResult(enrolTestRule: ActivityTestRule<CheckLoginFromIntentActivity>): String {
    log("enrolmentReturnedResult")
    val registration = enrolTestRule.activityResult
        .resultData.getParcelableExtra<Registration>(Constants.SIMPRINTS_REGISTRATION)
    val guid = registration.guid
    assertNotNull(guid)
    return guid
}

fun matchingActivityIdentificationCheckFinished(identifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>) {
    log("matchingActivityIdentificationCheckFinished")
    tryOnSystemUntilTimeout(UI_TIMEOUT, UI_POLLING_INTERVAL_LONG) {
        assertTrue(identifyTestRule.activity.isDestroyed)
    }
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

fun twoReturnedIdentificationsOneMatchOneNotMatch(identifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>,
                                            matchGuid: String,
                                            notMatchGuid: String) {
    log("twoReturnedIdentificationsOneMatchOneNotMatch")
    val identifications = identifyTestRule.activityResult
        .resultData.getParcelableArrayListExtra<Identification>(Constants.SIMPRINTS_IDENTIFICATIONS)
    assertEquals(2, identifications.size.toLong())

    assertEquals(matchGuid, identifications[0].guid)
    assertTrue(identifications[0].confidence > 0)
    assertNotEquals(Tier.TIER_5, identifications[0].tier)

    assertEquals(notMatchGuid, identifications[1].guid)
    assertTrue(identifications[1].confidence > 0)
    assertEquals(Tier.TIER_5, identifications[1].tier)
}

fun matchingActivityVerificationCheckFinished(verifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>) {
    log("matchingActivityVerificationCheckFinished")
    tryOnSystemUntilTimeout(UI_TIMEOUT, UI_POLLING_INTERVAL_LONG) {
        assertTrue(verifyTestRule.activity.isDestroyed)
    }
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
