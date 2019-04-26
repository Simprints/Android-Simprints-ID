package com.simprints.fingerprint.commontesttools.models

import com.simprints.fingerprint.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.fingerprint.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.fingerprint.commontesttools.DefaultTestConstants.DEFAULT_USER_ID

data class TestCalloutCredentials(val projectId: String = DEFAULT_PROJECT_ID,
                                  val moduleId: String = DEFAULT_MODULE_ID,
                                  val userId: String = DEFAULT_USER_ID)
