package com.simprints.id.domain.sessionParameters.calloutParameter.extras

import com.simprints.id.domain.callout.CalloutParameter
import com.simprints.id.domain.callout.CalloutParameters
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.testUtils.assertThrows
import com.simprints.libsimprints.Constants.SIMPRINTS_MODULE_ID
import org.junit.Assert
import org.junit.Test


class ModuleIdParameterTest {

    private val moduleId: String = "moduleId"

    private val moduleIdParam = CalloutParameter(SIMPRINTS_MODULE_ID, moduleId)

    private val emptyCalloutParameters = CalloutParameters(emptySet())
    private val calloutParametersWithModuleId = CalloutParameters(setOf(moduleIdParam))

    @Test
    fun testValidateThrowsErrorWhenValueIsMissing() {
        val moduleIdParameter = ModuleIdParameter(emptyCalloutParameters)
        val throwable = assertThrows<InvalidCalloutError> {
            moduleIdParameter.validate()
        }
        Assert.assertEquals(ALERT_TYPE.MISSING_MODULE_ID, throwable.alertType)
    }

    @Test
    fun testValidateSucceedsWhenValueIsNotMissing() {
        val moduleIdParameter = ModuleIdParameter(calloutParametersWithModuleId)
        moduleIdParameter.validate()
    }
    
}
