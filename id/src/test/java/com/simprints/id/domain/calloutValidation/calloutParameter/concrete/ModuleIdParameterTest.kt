package com.simprints.id.domain.calloutValidation.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.domain.calloutValidation.calloutParameter.mockIntent
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.testUtils.assertThrows
import com.simprints.libsimprints.Constants.SIMPRINTS_MODULE_ID
import org.junit.Assert
import org.junit.Test


class ModuleIdParameterTest {

    private val aModuleId: String = "aModuleId"

    private val emptyIntent: Intent = mockIntent()
    private val moduleIdIntent: Intent = mockIntent(SIMPRINTS_MODULE_ID to aModuleId)

    @Test
    fun testValidateThrowsErrorWhenValueIsMissing() {
        val moduleIdParameter = ModuleIdParameter(emptyIntent)
        val throwable = assertThrows<InvalidCalloutError> {
            moduleIdParameter.validate()
        }
        Assert.assertEquals(ALERT_TYPE.MISSING_MODULE_ID, throwable.alertType)
    }

    @Test
    fun testValidateSucceedsWhenValueIsNotMissing() {
        val moduleIdParameter = ModuleIdParameter(moduleIdIntent)
        moduleIdParameter.validate()
    }
    
}
