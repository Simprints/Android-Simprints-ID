package com.simprints.id.domain.sessionParameters.calloutParameter.extras

import com.simprints.id.domain.callout.CalloutParameter
import com.simprints.id.domain.callout.CalloutParameters
import com.simprints.libsimprints.Constants.SIMPRINTS_CALLING_PACKAGE
import org.junit.Test


class CallingPackageParameterTest {

    private val callingPackage = CalloutParameter(SIMPRINTS_CALLING_PACKAGE, "Any calling package")

    private val emptyCalloutParameters = CalloutParameters(emptySet())

    private val calloutParametersWithCallingPackage = CalloutParameters(setOf(callingPackage))

    @Test
    fun testValidateSucceedsWhenValueIsEmpty() {
        val callingPackageParameter = CallingPackageParameter(emptyCalloutParameters)
        callingPackageParameter.validate()
    }

    @Test
    fun testValidateSucceedsWithAnyValue() {
        val callingPackageParameter = CallingPackageParameter(calloutParametersWithCallingPackage)
        callingPackageParameter.validate()
    }

}
