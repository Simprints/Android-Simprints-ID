package com.simprints.id.testtools.adapters

import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.models.TestCalloutCredentials
import com.simprints.id.testtools.models.TestProject

fun TestProject.toCalloutCredentials(moduleId: String = DEFAULT_MODULE_ID,
                                     userId: String = DEFAULT_USER_ID): TestCalloutCredentials =
    TestCalloutCredentials(
        projectId = id,
        moduleId = moduleId,
        userId = userId,
        legacyApiKey = legacyId)
