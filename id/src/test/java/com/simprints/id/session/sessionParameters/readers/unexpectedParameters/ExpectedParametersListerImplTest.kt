package com.simprints.id.session.sessionParameters.readers.unexpectedParameters

import com.simprints.id.session.callout.Callout
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.session.callout.CalloutParameters
import shared.mock
import com.simprints.libsimprints.Constants
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpectedParametersListerImplTest {

    val commonExpectedKeys = setOf(
        Constants.SIMPRINTS_API_KEY,
        Constants.SIMPRINTS_MODULE_ID,
        Constants.SIMPRINTS_USER_ID,
        Constants.SIMPRINTS_RESULT_FORMAT,
        Constants.SIMPRINTS_METADATA,
        Constants.SIMPRINTS_CALLING_PACKAGE
    )

    val expectedParametersLister = ExpectedParametersListerImpl()

    val anyCalloutParameters = mock<CalloutParameters>()

    @Test
    fun testListExpectedParameterKeysFromVerificationCalloutContainsVerifyGuidKeyButNotUpdateGuidKey() {
        val verifyCallout = Callout(CalloutAction.VERIFY, anyCalloutParameters)
        val expectedParameterKeys = expectedParametersLister.listKeysOfExpectedParametersIn(verifyCallout)
        assertTrue(Constants.SIMPRINTS_VERIFY_GUID in expectedParameterKeys &&
            Constants.SIMPRINTS_UPDATE_GUID !in expectedParameterKeys)
    }

    @Test
    fun testListExpectedParameterKeysFromUpdateCalloutContainsUpdateGuidKeyButNotVerifyGuidKey() {
        val updateCallout = Callout(CalloutAction.UPDATE, anyCalloutParameters)
        val expectedParameterKeys = expectedParametersLister.listKeysOfExpectedParametersIn(updateCallout)
        assertTrue(Constants.SIMPRINTS_VERIFY_GUID !in expectedParameterKeys &&
            Constants.SIMPRINTS_UPDATE_GUID in expectedParameterKeys)
    }

    @Test
    fun testListExpectedParameterKeysFromAnyValidActionCalloutContainsCommonKeys() {
        CalloutAction.validValues
            .map { Callout(it, anyCalloutParameters) }
            .map { expectedParametersLister.listKeysOfExpectedParametersIn(it) }
            .forEach { assertTrue(it.containsAll(commonExpectedKeys)) }
    }

    @Test
    fun testListExpectedParameterKeysFromAnyInvalidActionCalloutIsEmpty() {
        CalloutAction.values()
            .filterNot { it in CalloutAction.validValues }
            .map { Callout(it, anyCalloutParameters) }
            .map { expectedParametersLister.listKeysOfExpectedParametersIn(it) }
            .forEach { assertTrue(it.isEmpty()) }
    }


}
