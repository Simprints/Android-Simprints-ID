package com.simprints.id.domain.calloutValidation.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.domain.calloutValidation.calloutParameter.mockIntent
import com.simprints.libsimprints.Constants.SIMPRINTS_CALLING_PACKAGE
import org.junit.Test


class CallingPackageParameterTest {

    private val anyCallingPackage: String = "Any calling package"

    private val emptyIntent: Intent = mockIntent()
    private val intentWithAnyCallingPackage: Intent =
            mockIntent(SIMPRINTS_CALLING_PACKAGE to anyCallingPackage)

    @Test
    fun testValidateSucceedsWhenValueIsEmpty() {
        val callingPackageParameter = CallingPackageParameter(emptyIntent)
        callingPackageParameter.validate()
    }

    @Test
    fun testValidateSucceedsWithAnyValue() {
        val callingPackageParameter = CallingPackageParameter(intentWithAnyCallingPackage)
        callingPackageParameter.validate()
    }

}
