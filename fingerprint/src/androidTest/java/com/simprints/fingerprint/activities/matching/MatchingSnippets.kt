package com.simprints.fingerprint.activities.matching

import androidx.test.rule.ActivityTestRule
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Tier
import com.simprints.libsimprints.Verification
import com.simprints.testtools.android.WaitingUtils
import com.simprints.testtools.android.log
import com.simprints.testtools.android.tryOnSystemUntilTimeout
import org.junit.Assert

fun matchingActivityIdentificationCheckFinished(identifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>) {
    log("matchingActivityIdentificationCheckFinished")
    tryOnSystemUntilTimeout(WaitingUtils.UI_TIMEOUT, WaitingUtils.UI_POLLING_INTERVAL_LONG) {
        Assert.assertTrue(identifyTestRule.activity.isDestroyed)
    }
}

fun guidIsTheOnlyReturnedIdentification(identifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>, guid: String) {
    log("guidIsTheOnlyReturnedIdentification")
    val identifications = identifyTestRule.activityResult
        .resultData.getParcelableArrayListExtra<Identification>(Constants.SIMPRINTS_IDENTIFICATIONS)
    Assert.assertEquals(1, identifications.size.toLong())
    Assert.assertEquals(guid, identifications[0].guid)
    Assert.assertTrue(identifications[0].confidence > 0)
    Assert.assertNotEquals(Tier.TIER_5, identifications[0].tier)
}

fun twoReturnedIdentificationsOneMatchOneNotMatch(identifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>,
                                                  matchGuid: String,
                                                  notMatchGuid: String) {
    log("twoReturnedIdentificationsOneMatchOneNotMatch")
    val identifications = identifyTestRule.activityResult
        .resultData.getParcelableArrayListExtra<Identification>(Constants.SIMPRINTS_IDENTIFICATIONS)
    Assert.assertEquals(2, identifications.size.toLong())

    Assert.assertEquals(matchGuid, identifications[0].guid)
    Assert.assertTrue(identifications[0].confidence > 0)
    Assert.assertNotEquals(Tier.TIER_5, identifications[0].tier)

    Assert.assertEquals(notMatchGuid, identifications[1].guid)
    Assert.assertTrue(identifications[1].confidence > 0)
    Assert.assertEquals(Tier.TIER_5, identifications[1].tier)
}

fun matchingActivityVerificationCheckFinished(verifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>) {
    log("matchingActivityVerificationCheckFinished")
    tryOnSystemUntilTimeout(WaitingUtils.UI_TIMEOUT, WaitingUtils.UI_POLLING_INTERVAL_LONG) {
        Assert.assertTrue(verifyTestRule.activity.isDestroyed)
    }
}

fun verificationSuccessful(verifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>, guid: String) {
    log("verificationSuccessful")
    val verification = verifyTestRule.activityResult
        .resultData.getParcelableExtra<Verification>(Constants.SIMPRINTS_VERIFICATION)
    Assert.assertEquals(guid, verification.guid)
    Assert.assertTrue(verification.confidence > 0)
    Assert.assertNotEquals(Tier.TIER_5, verification.tier)
}

fun verificationNotAMatch(verifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>, guid: String) {
    log("verificationSuccessful")
    val verification = verifyTestRule.activityResult
        .resultData.getParcelableExtra<Verification>(Constants.SIMPRINTS_VERIFICATION)
    Assert.assertEquals(guid, verification.guid)
    Assert.assertTrue(verification.confidence > 0)
    Assert.assertEquals(Tier.TIER_5, verification.tier)
}
