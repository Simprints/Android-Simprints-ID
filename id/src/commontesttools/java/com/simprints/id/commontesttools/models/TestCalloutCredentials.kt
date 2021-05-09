package com.simprints.id.commontesttools.models

import com.simprints.id.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_USER_ID

data class TestCalloutCredentials(val projectId: String = DEFAULT_PROJECT_ID,
                                  val moduleId: String = DEFAULT_MODULE_ID,
                                  val userId: String = DEFAULT_USER_ID)
